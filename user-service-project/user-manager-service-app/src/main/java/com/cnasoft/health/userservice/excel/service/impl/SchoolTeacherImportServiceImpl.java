package com.cnasoft.health.userservice.excel.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.SchoolTeacherDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.INeedEncryptService;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.SchoolTeacherMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SchoolTeacher;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
 * 校心理老师导入方法
 *
 * @author Administrator
 */
@Component("schoolTeacherImportServiceImpl")
@Slf4j
public class SchoolTeacherImportServiceImpl extends ImportThreadPool implements IImportDataInterface<SchoolTeacherDTO> {

    @Value("${user.password.key}")
    private String key;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private ImportRecordMapper importRecordMapper;

    @Resource
    private SchoolTeacherMapper schoolTeacherMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private INeedEncryptService<SchoolTeacherDTO> needEncryptService;

    @Resource
    private FileFeignClient fileFeignClient;

    /**
     * 第一步
     *
     * @param param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<SchoolTeacherDTO> param, boolean intelligent, List<SchoolTeacherDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<SchoolTeacherDTO> schoolTeacherData;
        if (intelligent) {
            schoolTeacherData = list;
        } else {
            schoolTeacherData = param.getExcelLoadService().load(param.getFile(), SchoolTeacherDTO.class);
        }

        if (CollectionUtils.isEmpty(schoolTeacherData)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord record = new ImportRecord(param.getImportTypeEnum().getCode());
        record.setFileName(param.getFile().getOriginalFilename());
        record.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        record.setProgress(StudentConstant.PROCESSING);
        record.setTotalNum(schoolTeacherData.size());
        importRecordMapper.insert(record);

        //2、分批处理数据
        beanValid(param, record, schoolTeacherData);
        return record;
    }

    /**
     * 批量更新或插入校心理老师
     *
     * @param list
     */
    @Override
    public void saveBatch(List<SchoolTeacherDTO> list, ImportParam<SchoolTeacherDTO> importParam) throws Exception {
        List<Object> staffs = new ArrayList<>();
        //查询该区域是否已有任务承接人
        Long id = schoolTeacherMapper.getAcceptTaskId(importParam.getSchoolId());
        AtomicBoolean existAcceptTask;
        if (Objects.isNull(id)) {
            existAcceptTask = new AtomicBoolean(false);
        } else {
            existAcceptTask = new AtomicBoolean(true);
        }
        list.forEach(e -> {
            SchoolTeacher schoolTeacher = e.getSelf(importParam);
            if (existAcceptTask.get()) {
                if (Objects.isNull(schoolTeacher.getId()) || (Objects.nonNull(schoolTeacher.getId()) && !schoolTeacher.getId().equals(id))) {
                    schoolTeacher.setIsAcceptTask(false);
                }
            } else {
                if (Boolean.TRUE.equals(schoolTeacher.getIsAcceptTask())) {
                    existAcceptTask.set(true);
                }
            }
            staffs.add(schoolTeacher);
        });

        UserUtil.saveOrUpdateBatch(staffs, schoolTeacherMapper);
    }

    @Override
    public String errorWriteToExcel(IFileService<SchoolTeacherDTO> fileService, String fileName, List<SchoolTeacherDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, SchoolTeacherDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 转换数据,将Excel中的某些数据转换为码表
     *
     * @param schoolTeacher
     * @param dictMap
     */
    private void convertData(SchoolTeacherDTO schoolTeacher, Map<String, String> dictMap) {
        if (StringUtils.isNotBlank(schoolTeacher.getDepartment())) {
            String value = dictMap.get(schoolTeacher.getDepartment());
            if (StringUtils.isBlank(value)) {
                schoolTeacher.setErrorMsg("部门数据错误");
                schoolTeacher.setSuccess(false);
            } else {
                schoolTeacher.setDepartment(value);
            }
        }
    }

    /**
     * 获取校心理老师数据
     *
     * @param param
     * @param okList
     * @return
     * @throws InterruptedException
     */
    private List<SchoolTeacher> getSchoolTeacher(ImportParam<SchoolTeacherDTO> param, List<SchoolTeacherDTO> okList) throws InterruptedException {
        List<SchoolTeacher> schoolTeachers = new ArrayList<>();

        List<String> mobiles = okList.stream().map(SchoolTeacherDTO::getMobile).collect(Collectors.toList());
        if (CollUtil.isEmpty(mobiles)) {
            return schoolTeachers;
        }

        // 数据行数超过100,则分批次处理
        int count = mobiles.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    schoolTeachers.addAll(schoolTeacherMapper.selectBySchoolTeacher(param.getSchoolId(), mobiles.subList(i * batchCount, count), key));
                } else {
                    schoolTeachers.addAll(
                            schoolTeacherMapper.selectBySchoolTeacher(param.getSchoolId(), mobiles.subList(i * batchCount, (i + 1) * batchCount), key));
                }
            }
        } else {
            schoolTeachers.addAll(schoolTeacherMapper.selectBySchoolTeacher(param.getSchoolId(), mobiles, key));
        }

        return schoolTeachers;
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
    public void businessValid(ImportParam<SchoolTeacherDTO> param, List<SchoolTeacherDTO> okList, List<SchoolTeacherDTO> errList, ImportRecord record) throws Exception {
        List<SchoolTeacherDTO> updateData = new ArrayList<>();
        List<SchoolTeacherDTO> insertData = new ArrayList<>();

        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertData, updateData, errList, record);
            return;
        }

        //如果不能覆盖，就得判断数据库中是否有重复数据
        List<SchoolTeacher> dupSchoolTeachers = getSchoolTeacher(param, okList);

        //数据库里面重复的数据
        if (CollectionUtils.isNotEmpty(dupSchoolTeachers)) {

            Set<String> dupKeys = dupSchoolTeachers.stream().map(SchoolTeacher::getUsername).collect(Collectors.toSet());
            updateData = okList.stream().filter(e -> dupKeys.contains(e.getMobile())).collect(Collectors.toList());
            insertData = okList.stream().filter(e -> !dupKeys.contains(e.getMobile())).collect(Collectors.toList());

            //如果不覆盖，直接提示用户数据重复
            if (!param.getCover()) {
                for (SchoolTeacherDTO staffDTO : updateData) {
                    staffDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                    errList.add(staffDTO);
                }
                //如果不覆盖，就需要把重复数据加入错误数据集中
                updateData.clear();
            } else {
                //转为Map
                Map<String, Long> usernameAnduserIdMap =
                    dupSchoolTeachers.stream().collect(Collectors.toMap(SchoolTeacher::getUsername, SchoolTeacher::getUserId, (key1, key2) -> key2));
                Map<Long, Long> userIdAndTeacherIdMap = dupSchoolTeachers.stream().collect(Collectors.toMap(SchoolTeacher::getUserId, SchoolTeacher::getId, (key1, key2) -> key2));

                //查询出已经存在的Id
                updateData.forEach(e -> {
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
            insertData = new ArrayList<>(okList);
        }

        //获取学校部门数据
        Map<String, String> dictMap = UserUtil.getDictData(UserUtil.SCHOOL_DEPARTMENT);

        // 数据转换
        if (CollectionUtils.isNotEmpty(insertData)) {
            insertData.forEach(schoolTeacher -> convertData(schoolTeacher, dictMap));

            // 将转后的数据进行筛选，错误的进入错误列表
            List<SchoolTeacherDTO> errorList = new ArrayList<>();
            for (SchoolTeacherDTO schoolTeacher : insertData) {
                if (!schoolTeacher.success()) {
                    errorList.add(schoolTeacher);
                }
            }
            errList.addAll(errorList);
            insertData.removeAll(errorList);
        }

        if (CollectionUtils.isNotEmpty(updateData)) {
            updateData.forEach(schoolTeacher -> convertData(schoolTeacher, dictMap));

            // 将转后的数据进行筛选，错误的进入错误列表
            List<SchoolTeacherDTO> errorList = new ArrayList<>();
            for (SchoolTeacherDTO schoolTeacher : updateData) {
                if (!schoolTeacher.success()) {
                    errorList.add(schoolTeacher);
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
     * @param insertData
     * @param updateData
     * @param errList
     * @param record
     */
    @Override
    public void updateData(ImportParam<SchoolTeacherDTO> resultParams, List<SchoolTeacherDTO> insertData, List<SchoolTeacherDTO> updateData, List<SchoolTeacherDTO> errList,
        ImportRecord record) throws Exception {
        //分别处理返回回来的数据
        if (CollectionUtils.isNotEmpty(updateData)) {
            List<Object> userData = new ArrayList<>();
            updateData.forEach(e -> {
                SysUser user = e.getSysUser(resultParams);
                userData.add(user);
            });

            //批量保存用户
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            // 批量保存校心理老师
            saveBatch(updateData, resultParams);
        }

        if (CollectionUtils.isNotEmpty(insertData)) {
            List<Object> userData = new ArrayList<>();
            insertData.forEach(e -> {
                needEncryptService.encrypt(e.password());
                //先存储SysUser,然后获取userId,再存储角色和扩展信息
                userData.add(e.getSysUser(resultParams));
            });

            //批量保存用户
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            //转换成Map
            List<SysUser> userList = (List<SysUser>)(List)userData;
            Map<String, Long> idsMap = userList.stream().collect(Collectors.toMap(SysUser::getMobile, SysUser::getId, (key1, key2) -> key2));

            //遍历,把保存后的用户userId取过来做关联
            insertData.forEach(e -> {
                val userId = idsMap.get(e.getMobile());
                if (Objects.nonNull(userId)) {
                    e.setUserId(userId);
                }
            });

            // 批量保存校心理老师
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
    public void beanValid(ImportParam<SchoolTeacherDTO> param, ImportRecord record, List<SchoolTeacherDTO> list) throws Exception {
        List<SchoolTeacherDTO> okList = new ArrayList<>();
        List<SchoolTeacherDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<SchoolTeacherDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<SchoolTeacherDTO> dupList = map.get(false);
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
        List<SchoolTeacherDTO> okList2 = map.get(true);
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
    public Map<Boolean, List<SchoolTeacherDTO>> duplicate(List<SchoolTeacherDTO> sourceList) {
        Map<Boolean, List<SchoolTeacherDTO>> resultMap = new HashMap<>(16);
        List<SchoolTeacherDTO> okList = new ArrayList<>();
        List<SchoolTeacherDTO> errList = new ArrayList<>();

        Map<String, List<SchoolTeacherDTO>> map = sourceList.stream().collect(Collectors.groupingBy(SchoolTeacherDTO::getMobile));

        map.forEach((mobile, schoolTeacherDTOS) -> {
            if (schoolTeacherDTOS.size() > 1) {
                okList.add(schoolTeacherDTOS.get(0));
                errList.addAll(schoolTeacherDTOS.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(schoolTeacherDTOS);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }
}
