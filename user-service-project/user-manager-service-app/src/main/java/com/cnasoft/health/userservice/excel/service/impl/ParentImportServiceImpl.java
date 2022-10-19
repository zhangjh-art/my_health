package com.cnasoft.health.userservice.excel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cnasoft.health.common.enums.Relationship;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.ParentDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.INeedEncryptService;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.StudentBaseInfoMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Component("parentImportServiceImpl")
@Slf4j
public class ParentImportServiceImpl extends ImportThreadPool implements IImportDataInterface<ParentDTO> {

    private static final String ID_CARD_ERR_MSG = ";身份证号码格式有误或对应学生不存在";

    @Value("${user.password.key}")
    private String key;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    private ImportRecordMapper importRecordMapper;

    @Resource
    private StudentBaseInfoMapper studentBaseInfoMapper;

    @Resource
    private ParentMapper parentMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private INeedEncryptService<ParentDTO> needEncryptService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<ParentDTO> param, boolean intelligent, List<ParentDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<ParentDTO> parentData;
        if (intelligent) {
            parentData = list;
        } else {
            parentData = param.getExcelLoadService().load(param.getFile(), ParentDTO.class);
        }

        if (CollectionUtils.isEmpty(parentData)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord record = new ImportRecord(param.getImportTypeEnum().getCode());
        record.setFileName(param.getFile().getOriginalFilename());
        record.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        record.setProgress(StudentConstant.PROCESSING);
        record.setTotalNum(parentData.size());
        importRecordMapper.insert(record);

        //2、分批处理数据
        beanValid(param, record, parentData);
        return record;
    }

    /**
     * 插入用户表，插入教职工表，插入角色表
     *
     * @param list
     */
    @Override
    public void saveBatch(List<ParentDTO> list, ImportParam<ParentDTO> importParam) throws Exception {
        List<Object> parents = new ArrayList<>();
        list.forEach(e -> parents.add(e.getSelf(importParam)));

        UserUtil.saveOrUpdateBatch(parents, parentMapper);

        List<Parent> parentList = (List)parents;
        Map<String, Long> usernameAndParentIdMap = parentList.stream().collect(Collectors.toMap(Parent::getMobile, Parent::getId, (key1, key2) -> key2));

        list.forEach(parent -> {
            if (usernameAndParentIdMap.containsKey(parent.getMobile())) {
                parent.setId(usernameAndParentIdMap.get(parent.getMobile()));
            }
        });
    }

    @Override
    public String errorWriteToExcel(IFileService<ParentDTO> fileService, String fileName, List<ParentDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, ParentDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 转换数据,将Excel中的某些数据转换为码表
     *
     * @param parent
     */
    private void convertData(ParentDTO parent) {
        Relationship relationship;
        if (StringUtils.isNotBlank(parent.getRelationship())) {
            relationship = Relationship.getRelationship(parent.getRelationship());
            if (Objects.isNull(relationship)) {
                parent.setErrorMsg(StringUtils.isBlank(parent.getErrorMsg()) ? "家长关系填写错误" : parent.getErrorMsg() + ";家长关系填写错误");
                parent.setSuccess(false);
            } else {
                parent.setRelationship(String.valueOf(relationship.getCode()));
            }
        }
    }

    private List<Parent> getParent(ImportParam<ParentDTO> param, List<ParentDTO> okList) throws InterruptedException {
        List<Parent> parents = new ArrayList<>();
        List<String> mobiles = okList.stream().map(ParentDTO::getMobile).collect(Collectors.toList());
        if (CollUtil.isEmpty(mobiles)) {
            return parents;
        }

        // 数据行数超过100,则分批次处理
        int count = mobiles.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    parents.addAll(parentMapper.selectByParent(param.getSchoolId(), mobiles.subList(i * batchCount, count), key));
                } else {
                    parents.addAll(parentMapper.selectByParent(param.getSchoolId(), mobiles.subList(i * batchCount, (i + 1) * batchCount), key));
                }
            }
        } else {
            parents.addAll(parentMapper.selectByParent(param.getSchoolId(), mobiles, key));
        }
        return parents;
    }

    /**
     * 验证数据库里面是否有重复数据
     *
     * @param param   格式有误的数据
     * @param okList  格式验证通过的数据
     * @param errList 格式有误的数据
     * @param record  格式有误的数据
     */
    @Override
    public void businessValid(ImportParam<ParentDTO> param, List<ParentDTO> okList, List<ParentDTO> errList, ImportRecord record) throws Exception {
        List<ParentDTO> updateData = new ArrayList<>();
        List<ParentDTO> insertData = new ArrayList<>();

        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertData, updateData, errList, record, null);
            return;
        }

        // 当学生身份证号不为空时，关系也不能为空 todo 这段代码逻辑有问题，但具体修改需要参照业务需求,循环外的if判断了idCardNumber，循环里又没有使用
        List<String> idCardNumbers = okList.stream().filter(r -> Objects.nonNull(r.getIdCardNumbers())).map(ParentDTO::getIdCardNumbers).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idCardNumbers)) {
            List<ParentDTO> tempLists = new ArrayList<>();
            okList.forEach(parent -> {
                if (StrUtil.isEmpty(parent.getRelationship())) {
                    if (org.apache.commons.lang3.StringUtils.isEmpty(parent.getErrorMsg())) {
                        parent.setErrorMsg("关系为空");
                    } else {
                        parent.setErrorMsg(parent.getErrorMsg() + ";" + "关系为空");
                    }
                    parent.setSuccess(false);

                    tempLists.add(parent);
                }
            });

            if (CollUtil.isNotEmpty(tempLists)) {
                errList.addAll(tempLists);
                okList.removeAll(tempLists);
            }
        }

        //校验学生身份证号码
        TwoTuple<List<String>, Map<String, StudentBaseInfo>> resultValue = idCardValid(param.getSchoolId(), okList, errList);
        //从 insertData,updateData集合中移除身份证号码错误的数据,加入到errList中
        List<String> errIdCards = resultValue.first;
        Map<String, StudentBaseInfo> stuInfoMap = resultValue.second;
        clean(errIdCards, okList, errList);

        //如果不能覆盖，就得判断数据库中是否有重复数据
        List<Parent> dupParents = getParent(param, okList);

        //数据库里面重复的数据
        if (CollUtil.isNotEmpty(dupParents)) {
            Set<String> dupKeys = dupParents.stream().map(Parent::getUsername).collect(Collectors.toSet());
            updateData = okList.stream().filter(e -> dupKeys.contains(e.getMobile())).collect(Collectors.toList());
            insertData = okList.stream().filter(e -> !dupKeys.contains(e.getMobile())).collect(Collectors.toList());

            //如果不覆盖，直接提示用户数据重复
            if (Boolean.FALSE.equals(param.getCover())) {
                for (ParentDTO staffDTO : updateData) {
                    staffDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                    errList.add(staffDTO);
                }
                //如果不覆盖，就需要把重复数据加入错误数据集中
                updateData.clear();

            } else {
                //转为Map
                Map<String, Long> usernameAnduserIdMap = dupParents.stream().collect(Collectors.toMap(Parent::getUsername, Parent::getUserId, (key1, key2) -> key2));
                Map<Long, Long> userIdAndParentIdMap = dupParents.stream().collect(Collectors.toMap(Parent::getUserId, Parent::getId, (key1, key2) -> key2));

                //查询出已经存在的Id
                updateData.forEach(e -> {
                    Long userId = usernameAnduserIdMap.get(e.getMobile());
                    if (Objects.nonNull(userId)) {
                        e.setUserId(userId);
                        Long parentId = userIdAndParentIdMap.get(userId);
                        if (Objects.nonNull(parentId)) {
                            e.setId(parentId);
                        }
                    }
                });
            }
        } else {
            insertData = new ArrayList<>(okList);
        }

        // 数据转换
        if (CollectionUtils.isNotEmpty(insertData)) {
            insertData.forEach(this::convertData);

            // 将转后的数据进行筛选，错误的进入错误列表
            List<ParentDTO> insertErrorList = new ArrayList<>();
            for (ParentDTO parent : insertData) {
                if (Boolean.FALSE.equals(parent.success())) {
                    insertErrorList.add(parent);
                }
            }
            errList.addAll(insertErrorList);
            insertData.removeAll(insertErrorList);
        }

        if (CollectionUtils.isNotEmpty(updateData)) {
            updateData.forEach(this::convertData);

            // 将转后的数据进行筛选，错误的进入错误列表
            List<ParentDTO> insertErrorList = new ArrayList<>();
            for (ParentDTO parent : updateData) {
                if (Boolean.FALSE.equals(parent.success())) {
                    insertErrorList.add(parent);
                }
            }
            errList.addAll(insertErrorList);
            updateData.removeAll(insertErrorList);
        }

        updateData(param, insertData, updateData, errList, record, stuInfoMap);
    }

    /**
     * 清理数据，根据errIdCards
     *
     * @param errIdCards
     * @param targetList
     * @param errList
     */
    void clean(List<String> errIdCards, List<ParentDTO> targetList, List<ParentDTO> errList) {
        if (errIdCards != null && !errIdCards.isEmpty()) {
            if (!targetList.isEmpty()) {
                List<ParentDTO> tempList = new ArrayList<>();
                for (String errIdCard : errIdCards) {
                    List<ParentDTO> errList1 =
                        targetList.stream().filter(e -> Objects.nonNull(e.getIdCardNumbers()) && e.getIdCardNumbers().contains(errIdCard)).collect(Collectors.toList());
                    if (!errList1.isEmpty()) {
                        tempList.addAll(errList1);
                        targetList.removeAll(errList1);
                    }
                    if (!errList.isEmpty()) {
                        //在格式错误的数据中，对不存在身份证号码的，同样标注身份证号码存在问题
                        List<ParentDTO> tempList1 =
                            errList.stream().filter(e -> Objects.nonNull(e.getIdCardNumbers()) && e.getIdCardNumbers().contains(errIdCard)).collect(Collectors.toList());
                        tempList1.forEach(e -> {
                            String msg = e.getErrorMsg();
                            if (msg == null) {
                                msg = "";
                            }
                            if (!msg.contains(ID_CARD_ERR_MSG)) {
                                e.setErrorMsg(String.format("%s%s", msg, ID_CARD_ERR_MSG));
                            }
                        });
                    }
                }

                if (!tempList.isEmpty()) {
                    tempList.forEach(e -> {
                        String msg = e.getErrorMsg();
                        if (msg == null) {
                            msg = "";
                        }
                        e.setErrorMsg(String.format("%s%s", msg, ID_CARD_ERR_MSG));
                    });
                    errList.addAll(tempList);
                }
            }
        }
    }

    /**
     * @param okList
     * @param errList
     * @return
     */
    TwoTuple<List<String>, Map<String, StudentBaseInfo>> idCardValid(Long schoolId, List<ParentDTO> okList, List<ParentDTO> errList) throws InterruptedException {
        //Code :根据学生身份证号码，查询是否存在学生
        List<String> idCardNums1 = okList.stream().map(ParentDTO::getIdCardNumbers).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> idCardNums2 = errList.stream().map(ParentDTO::getIdCardNumbers).filter(Objects::nonNull).collect(Collectors.toList());

        List<String> allIDCards = new ArrayList<>();
        List<String> errIDCards = new ArrayList<>();

        idCardNums1.addAll(idCardNums2);
        //把所有身份证号码处理一下,把多个身份证号码整理出来
        idCardNums1.forEach(e -> {
            String v = e.trim();
            if (!StringUtils.isEmpty(v)) {
                //把中文的分号改为英文分号
                v = v.replace("；", ";");
                if (v.contains(";")) {
                    String[] ids = v.split(";");
                    for (String id : ids) {
                        if (TextValidator.isIdCard(id)) {
                            allIDCards.add(id);
                        } else {
                            //格式不满足的
                            errIDCards.add(id);
                        }
                    }
                } else {
                    if (TextValidator.isIdCard(v)) {
                        allIDCards.add(v);
                    } else {
                        //格式不满足的
                        errIDCards.add(v);
                    }
                }
            }
        });

        Map<String, StudentBaseInfo> map = null;
        if (CollUtil.isNotEmpty(allIDCards)) {
            //查询出已经存在的学生信息
            List<StudentBaseInfo> list = new ArrayList<>();
            // 数据行数超过1000,则分批次处理
            int count = allIDCards.size();
            int batchCount = Constant.BATCH_COUNT;
            if (count > batchCount) {
                int times = (count + batchCount - 1) / batchCount;

                List<String> idCards;
                for (int i = 0; i < times; i++) {
                    if (i == times - 1) {
                        idCards = allIDCards.subList(i * batchCount, count);
                    } else {
                        idCards = allIDCards.subList(i * batchCount, (i + 1) * batchCount);
                    }
                    list.addAll(studentBaseInfoMapper.findIdCardNums(idCards, schoolId, key));
                }
            } else {
                list.addAll(studentBaseInfoMapper.findIdCardNums(allIDCards, schoolId, key));
            }

            if (CollectionUtils.isNotEmpty(list)) {
                map = list.stream().collect(Collectors.toMap(StudentBaseInfo::getIdentityCardNumber, e -> e, (key1, key2) -> key2));

                if (list.size() > 0 && list.size() != allIDCards.size()) {
                    //数据库中找不到对应学生的
                    List<String> temps = list.stream().map(StudentBaseInfo::getIdentityCardNumber).collect(Collectors.toList());

                    allIDCards.forEach(e -> {
                        if (!temps.contains(e)) {
                            errIDCards.add(e);
                        }
                    });
                }
            } else {
                errIDCards.addAll(allIDCards);
            }
        }

        return new TwoTuple<>(errIDCards, map);
    }

    @Override
    public void updateData(ImportParam<ParentDTO> resultParams, List<ParentDTO> insert, List<ParentDTO> update, List<ParentDTO> errList, ImportRecord record) {

    }

    /**
     * 处理数据，入库、更新数据库
     *
     * @param resultParams
     */
    public void updateData(ImportParam<ParentDTO> resultParams, List<ParentDTO> insertData, List<ParentDTO> updateData, List<ParentDTO> errList, ImportRecord record,
        Map<String, StudentBaseInfo> map) throws Exception {
        //分别处理返回回来的数据
        if (CollectionUtils.isNotEmpty(updateData)) {
            List<SysUser> sysUsers = new ArrayList<>();
            updateData.forEach(e -> sysUsers.add(e.getSysUser(resultParams)));

            List<Object> parentList = sysUsers.stream().filter(e -> Objects.nonNull(e.getId())).collect(Collectors.toList());
            UserUtil.saveOrUpdateBatch(parentList, sysUserMapper);

            // 批量保存家长信息
            saveBatch(updateData, resultParams);
        }

        if (CollectionUtils.isNotEmpty(insertData)) {
            // 批量保存家长用户
            List<Object> userData = new ArrayList<>();
            insertData.forEach(e -> {
                needEncryptService.encrypt(e.password());
                //先存储SysUser,然后获取userId,在存储角色和扩展信息
                userData.add(e.getSysUser(resultParams));
            });
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            List<SysUser> sysUsers = (List<SysUser>)(List)userData;

            Map<String, Long> idsMap = sysUsers.stream().collect(Collectors.toMap(SysUser::getMobile, SysUser::getId, (key1, key2) -> key2));
            insertData.forEach(e -> {
                Long userId = idsMap.get(e.getMobile());
                if (Objects.nonNull(userId)) {
                    e.setUserId(userId);
                }
            });

            // 批量保存家长信息
            saveBatch(insertData, resultParams);
        }

        List<ParentDTO> parentDTOList = new ArrayList<>(updateData);
        parentDTOList.addAll(insertData);

        if (ObjectUtils.isNotEmpty(map)) {
            updateParentIdByIdCardNum(parentDTOList, map, resultParams);
        }

        record.setSuccessNum(parentDTOList.size());
        record.setFailNum(errList.size());
        record.setUpdateTime(resultParams.getDateInterface().now());
        record.setProgress(StudentConstant.FINISHED);

        if (CollectionUtils.isNotEmpty(errList)) {
            // 验证未通过的数据生成excel，供下载用
            String downloadUrl = errorWriteToExcel(resultParams.getFileService(), resultParams.getErrorFilePath(), errList);
            record.setFailPath(downloadUrl);
        }

        record.setUpdateBy(resultParams.getUpdateBy());
        importRecordMapper.updateById(record);
    }

    /**
     * 更新学生表中的家长信息
     *
     * @param stuMap
     */
    void updateParentIdByIdCardNum(List<ParentDTO> parentDTOList, Map<String, StudentBaseInfo> stuMap, ImportParam<ParentDTO> resultParams) {
        List<StudentBaseInfo> stuList = new ArrayList<>();
        parentDTOList.forEach(e -> {
            for (String key : stuMap.keySet()) {
                if (e.getIdCardNumbers() != null && e.getIdCardNumbers().contains(key)) {
                    StudentBaseInfo stu = stuMap.get(key);
                    if (stu != null) {
                        stu.setRelationship(Integer.parseInt(e.getRelationship()));
                        stu.setParentId(e.getId());
                        stu.setUpdateBy(resultParams.getUpdateBy());
                        stu.setUpdateTime(resultParams.getDateInterface().now());
                        stuList.add(stu);
                        break;
                    }
                }
            }
        });

        if (stuList.size() > 0) {
            studentBaseInfoMapper.updateParentIdByIdCardNum(stuList);
        }
    }

    /**
     * 验证数据格式
     *
     * @param param
     * @param record
     * @param list
     * @return
     */
    @Override
    public void beanValid(ImportParam<ParentDTO> param, ImportRecord record, List<ParentDTO> list) throws Exception {
        List<ParentDTO> okList = new ArrayList<>();
        List<ParentDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<ParentDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<ParentDTO> dupList = map.get(false);
        dupList.forEach(e -> e.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage()));
        if (!dupList.isEmpty()) {
            errList.addAll(dupList);
        }
        //去掉重复的数据
        List<ParentDTO> okList2 = map.get(true);
        businessValid(param, okList2, errList, record);
    }

    /**
     * 查询结果：返回已经存在的KEYS
     *
     * @param keys
     */
    @Override
    public List<SysUser> unique(List<String> keys) {
        return Collections.emptyList();
    }

    /**
     * 验证导入数据中，是否有重复数据
     *
     * @param sourceList
     * @return
     */
    @Override
    public Map<Boolean, List<ParentDTO>> duplicate(List<ParentDTO> sourceList) {
        Map<Boolean, List<ParentDTO>> resultMap = new HashMap<>(16);
        List<ParentDTO> okList = new ArrayList<>();
        List<ParentDTO> errList = new ArrayList<>();

        Map<String, List<ParentDTO>> map = sourceList.stream().collect(Collectors.groupingBy(ParentDTO::getMobile));

        map.forEach((mobile, parentData) -> {
            if (parentData.size() > 1) {
                okList.add(parentData.get(0));
                errList.addAll(parentData.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(parentData);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }

    /**
     * 两个元素的元组，用于在一个方法里返回两种类型的值
     */
    static final class TwoTuple<A, B> {
        public final A first;
        public final B second;

        public TwoTuple(A a, B b) {
            this.first = a;
            this.second = b;
        }
    }
}