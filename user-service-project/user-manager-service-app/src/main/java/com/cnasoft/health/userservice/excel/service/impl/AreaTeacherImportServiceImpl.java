package com.cnasoft.health.userservice.excel.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.AreaTeacherDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.INeedEncryptService;
import com.cnasoft.health.userservice.mapper.AreaTeacherMapper;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.AreaTeacher;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 区域心理教研员导入实现类
 *
 * @author Administrator
 */
@Component("areaTeacherImportServiceImpl")
@Slf4j
public class AreaTeacherImportServiceImpl extends ImportThreadPool implements IImportDataInterface<AreaTeacherDTO> {

    @Value("${user.password.key}")
    private String key;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private ImportRecordMapper importRecordMapper;

    @Resource
    private AreaTeacherMapper areaTeacherMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private INeedEncryptService<AreaTeacherDTO> needEncryptService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<AreaTeacherDTO> param, boolean intelligent, List<AreaTeacherDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<AreaTeacherDTO> areaTeacherData;
        if (intelligent) {
            areaTeacherData = list;
        } else {
            areaTeacherData = param.getExcelLoadService().load(param.getFile(), AreaTeacherDTO.class);
        }

        if (CollectionUtils.isEmpty(areaTeacherData)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord importRecord = new ImportRecord(param.getImportTypeEnum().getCode());
        importRecord.setFileName(param.getFile().getOriginalFilename());
        importRecord.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        importRecord.setProgress(StudentConstant.PROCESSING);
        importRecord.setTotalNum(areaTeacherData.size());
        importRecordMapper.insert(importRecord);

        //2、分批处理数据
        beanValid(param, importRecord, areaTeacherData);
        return importRecord;
    }

    /**
     * 批量更新或插入区域心理教研员
     *
     * @param list 数据列表
     */
    @Override
    public void saveBatch(List<AreaTeacherDTO> list, ImportParam<AreaTeacherDTO> importParam) throws Exception {
        List<Object> data = new ArrayList<>();
        //查询该区域是否已有任务承接人
        Long id = areaTeacherMapper.getAcceptTaskId(importParam.getAreaCode());
        AtomicBoolean existAcceptTask;
        if (Objects.isNull(id)) {
            existAcceptTask = new AtomicBoolean(false);
        } else {
            existAcceptTask = new AtomicBoolean(true);
        }

        list.forEach(e -> {
            AreaTeacher areaTeacher = e.getSelf(importParam);
            if (existAcceptTask.get()) {
                if (Objects.isNull(areaTeacher.getId()) || (Objects.nonNull(areaTeacher.getId()) && !areaTeacher.getId().equals(id))) {
                    areaTeacher.setIsAcceptTask(false);
                }
            } else {
                if (Boolean.TRUE.equals(areaTeacher.getIsAcceptTask())) {
                    existAcceptTask.set(true);
                }
            }
            data.add(areaTeacher);
        });

        //批量添加或更新数据
        UserUtil.saveOrUpdateBatch(data, areaTeacherMapper);
    }

    @Override
    public String errorWriteToExcel(IFileService<AreaTeacherDTO> fileService, String fileName, List<AreaTeacherDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, AreaTeacherDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private List<AreaTeacher> getAreaTeacher(ImportParam<AreaTeacherDTO> param, List<AreaTeacherDTO> okList) throws InterruptedException {
        List<AreaTeacher> areaTeachers = new ArrayList<>();

        List<String> mobiles = okList.stream().map(AreaTeacherDTO::getMobile).collect(Collectors.toList());
        if (CollUtil.isEmpty(mobiles)) {
            return areaTeachers;
        }

        // 数据行数超过100,则分批次处理
        int count = mobiles.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    areaTeachers.addAll(
                                areaTeacherMapper.selectByAreaTeacher(param.getAreaCode(), RoleEnum.region_psycho_teacher.getValue(), mobiles.subList(i * batchCount, count),
                                    key));
                } else {
                    areaTeachers.addAll(areaTeacherMapper.selectByAreaTeacher(param.getAreaCode(), RoleEnum.region_psycho_teacher.getValue(),
                                mobiles.subList(i * batchCount, (i + 1) * batchCount), key));
                }
            }
        } else {
            areaTeachers.addAll(areaTeacherMapper.selectByAreaTeacher(param.getAreaCode(), RoleEnum.region_psycho_teacher.getValue(), mobiles, key));
        }

        return areaTeachers;
    }

    /**
     * 验证数据库里面是否有重复数据
     *
     * @param param        格式有误的数据
     * @param okList       格式验证通过的数据
     * @param errList      格式有误的数据
     * @param importRecord 格式有误的数据
     */
    @Override
    public void businessValid(ImportParam<AreaTeacherDTO> param, List<AreaTeacherDTO> okList, List<AreaTeacherDTO> errList, ImportRecord importRecord) throws Exception {
        List<AreaTeacherDTO> updateDTOList = new ArrayList<>();
        List<AreaTeacherDTO> insertDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertDTOList, updateDTOList, errList, importRecord);
            return;
        }

        //如果不能覆盖，就得判断数据库中是否有重复数据
        List<AreaTeacher> dupAreaTeachers = getAreaTeacher(param, okList);

        //数据库里面重复的数据
        if (CollectionUtils.isNotEmpty(dupAreaTeachers)) {

            Set<String> dupKeys = dupAreaTeachers.stream().map(AreaTeacher::getUsername).collect(Collectors.toSet());
            updateDTOList = okList.stream().filter(e -> dupKeys.contains(e.getMobile())).collect(Collectors.toList());
            insertDTOList = okList.stream().filter(e -> !dupKeys.contains(e.getMobile())).collect(Collectors.toList());

            //如果不覆盖，直接提示用户数据重复
            if (Boolean.FALSE.equals(param.getCover())) {
                for (AreaTeacherDTO staffDTO : updateDTOList) {
                    staffDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                    errList.add(staffDTO);
                }
                //如果不覆盖，就需要把重复数据加入错误数据集中
                updateDTOList.clear();
            } else {
                //转为Map
                Map<String, Long> usernameAnduserIdMap = dupAreaTeachers.stream().collect(Collectors.toMap(AreaTeacher::getUsername, AreaTeacher::getUserId, (key1, key2) -> key2));
                Map<Long, Long> userIdAndTeacherIdMap = dupAreaTeachers.stream().collect(Collectors.toMap(AreaTeacher::getUserId, AreaTeacher::getId, (key1, key2) -> key2));

                //查询出已经存在的Id
                updateDTOList.forEach(e -> {
                    Long userId = usernameAnduserIdMap.get(e.getMobile());
                    if (Objects.nonNull(userId)) {
                        e.setUserId(userId);
                        Long teacherId = userIdAndTeacherIdMap.get(userId);
                        if (Objects.nonNull(teacherId)) {
                            e.setId(teacherId);
                        }
                    }
                });
            }
        } else {
            insertDTOList = new ArrayList<>(okList);
        }

        updateData(param, insertDTOList, updateDTOList, errList, importRecord);
    }

    /**
     * 处理数据，入库、更新数据库
     *
     * @param resultParams 参数列表
     */
    @Override
    public void updateData(ImportParam<AreaTeacherDTO> resultParams, List<AreaTeacherDTO> insertDTOList, List<AreaTeacherDTO> updateDTOList, List<AreaTeacherDTO> errList,
        ImportRecord importRecord) throws Exception {
        //分别处理返回回来的数据
        if (CollectionUtils.isNotEmpty(updateDTOList)) {
            List<Object> userData = new ArrayList<>();
            updateDTOList.forEach(e -> userData.add(e.getSysUser(resultParams)));
            // 批量保存用户
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            // 批量保存区域心理教研员
            saveBatch(updateDTOList, resultParams);
        }

        if (CollectionUtils.isNotEmpty(insertDTOList)) {
            List<Object> userData = new ArrayList<>();
            insertDTOList.forEach(e -> {
                needEncryptService.encrypt(e.password());
                //先存储SysUser,然后获取userId,在存储角色和扩展信息
                userData.add(e.getSysUser(resultParams));
            });
            // 批量保存用户
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            //转换成Map
            List<SysUser> userList = (List)userData;
            Map<String, Long> idsMap = userList.stream().collect(Collectors.toMap(SysUser::getMobile, SysUser::getId, (key1, key2) -> key2));

            //遍历,把保存后的用户userId取过来做关联
            insertDTOList.forEach(e -> {
                Long userId = idsMap.get(e.getMobile());
                if (Objects.nonNull(userId)) {
                    e.setUserId(userId);
                }
            });

            // 批量保存区域心理教研员
            saveBatch(insertDTOList, resultParams);
        }

        int successNum = 0;
        if (CollectionUtils.isNotEmpty(insertDTOList)) {
            successNum += insertDTOList.size();
        }
        if (CollectionUtils.isNotEmpty(updateDTOList)) {
            successNum += updateDTOList.size();
        }

        importRecord.setSuccessNum(successNum);
        importRecord.setFailNum(errList.size());
        importRecord.setUpdateTime(resultParams.getDateInterface().now());
        importRecord.setProgress(StudentConstant.FINISHED);

        if (CollectionUtils.isNotEmpty(errList)) {
            String downloadUrl = errorWriteToExcel(resultParams.getFileService(), resultParams.getErrorFilePath(), errList);
            //验证未通过的数据生成excel，供下载用
            importRecord.setFailPath(downloadUrl);
        }

        importRecord.setUpdateBy(resultParams.getUpdateBy());
        importRecordMapper.updateById(importRecord);
    }

    /**
     * 验证数据格式
     *
     * @param param        参数
     * @param importRecord 导入记录
     * @param list         数据列表
     */
    @Override
    public void beanValid(ImportParam<AreaTeacherDTO> param, ImportRecord importRecord, List<AreaTeacherDTO> list) throws Exception {
        List<AreaTeacherDTO> okList = new ArrayList<>();
        List<AreaTeacherDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<AreaTeacherDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<AreaTeacherDTO> dupList = map.get(false);
        dupList.forEach(e -> {
            if (StringUtils.isEmpty(e.getErrorMsg())) {
                e.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
            } else {
                e.setErrorMsg(e.getErrorMsg() + ";" + StudentErrorCodeConstant.DUPLICATE.getMessage());
            }
        });

        if (!dupList.isEmpty()) {
            errList.addAll(dupList);
        }

        //去掉重复的数据
        List<AreaTeacherDTO> okList2 = map.get(true);
        businessValid(param, okList2, errList, importRecord);
    }

    /**
     * 查询结果：返回已经存在的KEYS
     *
     * @param mobiles 手机号列表
     */
    @Override
    public List<SysUser> unique(List<String> mobiles) {
        return Collections.emptyList();
    }

    /**
     * 验证导入数据中，是否有重复数据
     *
     * @param sourceList 数据源列表
     * @return 去重结果
     */
    @Override
    public Map<Boolean, List<AreaTeacherDTO>> duplicate(List<AreaTeacherDTO> sourceList) {
        Map<Boolean, List<AreaTeacherDTO>> resultMap = new HashMap<>(16);
        List<AreaTeacherDTO> okList = new ArrayList<>();
        List<AreaTeacherDTO> errList = new ArrayList<>();

        Map<String, List<AreaTeacherDTO>> map = sourceList.stream().collect(Collectors.groupingBy(AreaTeacherDTO::getMobile));

        map.forEach((mobile, areaTeacherData) -> {
            if (areaTeacherData.size() > 1) {
                okList.add(areaTeacherData.get(0));
                errList.addAll(areaTeacherData.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(areaTeacherData);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }

}