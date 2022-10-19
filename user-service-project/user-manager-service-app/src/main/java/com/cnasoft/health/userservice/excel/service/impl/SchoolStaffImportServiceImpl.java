package com.cnasoft.health.userservice.excel.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.SchoolStaffType;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.SchoolStaffDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.INeedEncryptService;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.SchoolStaffMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SchoolStaff;
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
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 校教职工导入方法
 *
 * @author Administrator
 */
@Component("schoolStaffImportServiceImpl")
@Slf4j
public class SchoolStaffImportServiceImpl extends ImportThreadPool implements IImportDataInterface<SchoolStaffDTO> {

    @Value("${user.password.key}")
    private String key;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    private ImportRecordMapper importRecordMapper;

    @Resource
    private SchoolStaffMapper schoolStaffMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private INeedEncryptService<SchoolStaffDTO> needEncryptService;

    /**
     * 第一步
     *
     * @param param 请求参数
     * @return 导入记录
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<SchoolStaffDTO> param, boolean intelligent, List<SchoolStaffDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<SchoolStaffDTO> schoolStaffData;
        if (intelligent) {
            schoolStaffData = list;
        } else {
            schoolStaffData = param.getExcelLoadService().load(param.getFile(), SchoolStaffDTO.class);
        }

        if (CollectionUtils.isEmpty(schoolStaffData)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord record = new ImportRecord(param.getImportTypeEnum().getCode());
        record.setFileName(param.getFile().getOriginalFilename());
        record.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        record.setProgress(StudentConstant.PROCESSING);
        record.setTotalNum(schoolStaffData.size());
        importRecordMapper.insert(record);

        //2、分批处理数据
        beanValid(param, record, schoolStaffData);
        return record;
    }

    /**
     * 批量更新或插入校教职工
     *
     * @param list
     */
    @Override
    public void saveBatch(List<SchoolStaffDTO> list, ImportParam<SchoolStaffDTO> importParam) throws Exception {
        List<Object> staffs = new ArrayList<>();
        list.forEach(e -> staffs.add(e.getSelf(importParam)));

        UserUtil.saveOrUpdateBatch(staffs, schoolStaffMapper);
    }

    @Override
    public String errorWriteToExcel(IFileService<SchoolStaffDTO> fileService, String fileName, List<SchoolStaffDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, SchoolStaffDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 转换数据,将Excel中的某些数据转换为码表
     *
     * @param schoolStaff
     * @param dictMap
     */
    private void convertData(SchoolStaffDTO schoolStaff, Map<String, String> dictMap) {
        if (StringUtils.isNotBlank(schoolStaff.getDepartment())) {
            String value = dictMap.get(schoolStaff.getDepartment());
            if (StringUtils.isBlank(value)) {
                schoolStaff.setErrorMsg("部门数据错误");
                schoolStaff.setSuccess(false);
            } else {
                schoolStaff.setDepartment(value);
            }
        }
    }

    private List<SchoolStaff> getSchoolStaff(ImportParam<SchoolStaffDTO> param, List<SchoolStaffDTO> okList, SchoolStaffType staffType) throws InterruptedException {
        List<SchoolStaff> schoolStaffs = new ArrayList<>();
        String roleCode;
        if (staffType.getCode() == 1) {
            roleCode = RoleEnum.school_staff.getValue();
        } else if (staffType.getCode() == 2) {
            roleCode = RoleEnum.school_head_teacher.getValue();
        } else {
            roleCode = RoleEnum.school_leader.getValue();
        }

        List<String> mobiles = okList.stream().filter(r -> r.getStaffType().equals(staffType.getName())).map(SchoolStaffDTO::getMobile).collect(Collectors.toList());
        if (CollUtil.isEmpty(mobiles)) {
            return schoolStaffs;
        }

        // 数据行数超过100,则分批次处理
        int count = mobiles.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    schoolStaffs.addAll(schoolStaffMapper.selectBySchoolStaff(param.getSchoolId(), roleCode, mobiles.subList(i * batchCount, count), key));
                } else {
                    schoolStaffs.addAll(
                            schoolStaffMapper.selectBySchoolStaff(param.getSchoolId(), roleCode, mobiles.subList(i * batchCount, (i + 1) * batchCount), key));
                }
            }
        } else {
            schoolStaffs.addAll(schoolStaffMapper.selectBySchoolStaff(param.getSchoolId(), roleCode, mobiles, key));
        }
        return schoolStaffs;
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
    public void businessValid(ImportParam<SchoolStaffDTO> param, List<SchoolStaffDTO> okList, List<SchoolStaffDTO> errList, ImportRecord record) throws Exception {
        List<SchoolStaffDTO> updateData = new ArrayList<>();
        List<SchoolStaffDTO> insertData = new ArrayList<>();
        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertData, updateData, errList, record);
            return;
        }

        //如果不能覆盖，就得判断数据库中是否有重复数据
        List<SchoolStaff> dupSchoolStaffs = new ArrayList<>();
        //查询教职工数据
        dupSchoolStaffs.addAll(getSchoolStaff(param, okList, SchoolStaffType.NORMAL_STAFF));
        //查询班主任数据
        dupSchoolStaffs.addAll(getSchoolStaff(param, okList, SchoolStaffType.HEAD_MASTER));
        //查询领导数据
        dupSchoolStaffs.addAll(getSchoolStaff(param, okList, SchoolStaffType.LEADER));

        //获取学校部门数据
        Map<String, String> dictMap = UserUtil.getDictData(UserUtil.SCHOOL_DEPARTMENT);

        //数据库里面重复的数据
        if (CollectionUtils.isNotEmpty(dupSchoolStaffs)) {

            Set<String> dupKeys = dupSchoolStaffs.stream().map(SchoolStaff::getUsername).collect(Collectors.toSet());
            updateData = okList.stream().filter(e -> dupKeys.contains(e.getMobile())).collect(Collectors.toList());
            insertData = okList.stream().filter(e -> !dupKeys.contains(e.getMobile())).collect(Collectors.toList());

            //如果不覆盖，直接提示用户数据重复
            if (Boolean.FALSE.equals(param.getCover())) {
                for (SchoolStaffDTO staffDTO : updateData) {
                    staffDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                    errList.add(staffDTO);
                }
                //如果不覆盖，就需要把重复数据加入错误数据集中
                updateData.clear();
            } else {
                //转为Map
                Map<String, Long> usernameAnduserIdMap = dupSchoolStaffs.stream().collect(Collectors.toMap(SchoolStaff::getUsername, SchoolStaff::getUserId, (key1, key2) -> key2));
                Map<Long, Long> userIdAndStaffIdMap = dupSchoolStaffs.stream().collect(Collectors.toMap(SchoolStaff::getUserId, SchoolStaff::getId, (key1, key2) -> key2));

                //查询出已经存在的Id
                updateData.forEach(e -> {
                    Long userId = usernameAnduserIdMap.get(e.getMobile());
                    if (Objects.nonNull(userId)) {
                        e.setUserId(userId);
                        Long staffId = userIdAndStaffIdMap.get(userId);
                        if (Objects.nonNull(staffId)) {
                            e.setId(staffId);
                        }
                    }
                });
            }
        } else {
            insertData = new ArrayList<>(okList);
        }

        // 数据转换
        if (CollectionUtils.isNotEmpty(insertData)) {
            insertData.forEach(schoolStaff -> convertData(schoolStaff, dictMap));

            // 将转后的数据进行筛选，错误的进入错误列表
            List<SchoolStaffDTO> errorList = new ArrayList<>();
            for (SchoolStaffDTO schoolStaff : insertData) {
                if (Boolean.FALSE.equals(schoolStaff.success())) {
                    errorList.add(schoolStaff);
                }
            }

            errList.addAll(errorList);
            insertData.removeAll(errorList);
        }

        if (CollectionUtils.isNotEmpty(updateData)) {
            updateData.forEach(schoolStaff -> convertData(schoolStaff, dictMap));

            // 将转后的数据进行筛选，错误的进入错误列表
            List<SchoolStaffDTO> errorList = new ArrayList<>();
            for (SchoolStaffDTO schoolStaff : updateData) {
                if (Boolean.FALSE.equals(schoolStaff.success())) {
                    errorList.add(schoolStaff);
                }
            }

            errList.addAll(errorList);
            updateData.removeAll(errorList);
        }

        updateData(param, insertData, updateData, errList, record);
    }

    /**
     * 处理数据，入库、更新数据库
     *
     * @param resultParams
     */
    @Override
    public void updateData(ImportParam<SchoolStaffDTO> resultParams, List<SchoolStaffDTO> insertData, List<SchoolStaffDTO> updateData, List<SchoolStaffDTO> errList,
        ImportRecord record) throws Exception {
        //分别处理返回回来的数据
        if (CollectionUtils.isNotEmpty(updateData)) {
            List<Object> userData = new ArrayList<>();
            updateData.forEach(e -> {
                SysUser user = e.getSysUser(resultParams);
                userData.add(user);
            });
            // 批量保存用户
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            // 批量保存校教职工
            saveBatch(updateData, resultParams);
        }

        if (CollectionUtils.isNotEmpty(insertData)) {
            List<Object> userData = new ArrayList<>();
            insertData.forEach(e -> {
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
            insertData.forEach(e -> {
                Long userId = idsMap.get(e.getMobile());
                if (Objects.nonNull(userId)) {
                    e.setUserId(userId);
                }
            });

            // 批量保存校教职工
            saveBatch(insertData, resultParams);
        }

        int successNum = 0;
        if (CollectionUtils.isNotEmpty(insertData)) {
            successNum += insertData.size();
        }
        if (CollectionUtils.isNotEmpty(updateData)) {
            successNum += updateData.size();
        }

        record.setSuccessNum(successNum);
        record.setFailNum(errList.size());
        record.setUpdateTime(resultParams.getDateInterface().now());
        record.setProgress(StudentConstant.FINISHED);

        if (CollectionUtils.isNotEmpty(errList)) {
            String downloadUrl = errorWriteToExcel(resultParams.getFileService(), resultParams.getErrorFilePath(), errList);
            //验证未通过的数据生成excel，供下载用
            record.setFailPath(downloadUrl);
        }

        record.setUpdateBy(resultParams.getUpdateBy());
        importRecordMapper.updateById(record);
    }

    /**
     * 验证数据格式
     *
     * @param param
     * @param record
     * @param list
     */
    @Override
    public void beanValid(ImportParam<SchoolStaffDTO> param, ImportRecord record, List<SchoolStaffDTO> list) throws Exception {
        List<SchoolStaffDTO> okList = new ArrayList<>();
        List<SchoolStaffDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<SchoolStaffDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<SchoolStaffDTO> dupList = map.get(false);
        dupList.forEach(e -> {
            if (StringUtils.isEmpty(e.getErrorMsg())) {
                e.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
            } else {
                e.setErrorMsg(e.getErrorMsg() + ";" + StudentErrorCodeConstant.DUPLICATE.getMessage());
            }
            e.setSuccess(false);
        });

        if (!dupList.isEmpty()) {
            errList.addAll(dupList);
        }

        //去掉重复的数据
        List<SchoolStaffDTO> okList2 = map.get(true);
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
     * @return 数据
     */
    @Override
    public Map<Boolean, List<SchoolStaffDTO>> duplicate(List<SchoolStaffDTO> sourceList) {
        Map<Boolean, List<SchoolStaffDTO>> resultMap = new HashMap<>(16);
        List<SchoolStaffDTO> okList = new ArrayList<>();
        List<SchoolStaffDTO> errList = new ArrayList<>();

        Map<String, List<SchoolStaffDTO>> map = sourceList.stream().collect(Collectors.groupingBy(SchoolStaffDTO::getMobile));

        map.forEach((mobile, schoolStaffData) -> {
            if (schoolStaffData.size() > 1) {
                okList.add(schoolStaffData.get(0));
                errList.addAll(schoolStaffData.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(schoolStaffData);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }
}
