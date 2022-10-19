package com.cnasoft.health.userservice.excel.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.enums.Relationship;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.StudentStatus;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.enums.BloodType;
import com.cnasoft.health.userservice.enums.DisabilityType;
import com.cnasoft.health.userservice.enums.FamilyCondition;
import com.cnasoft.health.userservice.enums.FamilyShort;
import com.cnasoft.health.userservice.enums.HealthyStatus;
import com.cnasoft.health.userservice.enums.LeftBehindChildren;
import com.cnasoft.health.userservice.enums.PoliticsStatus;
import com.cnasoft.health.userservice.enums.SpecialCondition;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.StudentDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.StudentAdditionalInfoMapper;
import com.cnasoft.health.userservice.mapper.StudentBaseInfoMapper;
import com.cnasoft.health.userservice.mapper.StudentFamilyConditionMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.util.RedisUtils;
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
 * @author Administrator
 * @date 2022/5/1 19:07
 **/
@Component("studentImportServiceImpl")
@Slf4j
public class StudentImportServiceImpl extends ImportThreadPool implements IImportDataInterface<StudentDTO> {

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
    private IClazzService clazzService;

    @Resource
    private StudentAdditionalInfoMapper studentAdditionalInfoMapper;

    @Resource
    private StudentFamilyConditionMapper studentFamilyConditionMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private ParentMapper parentMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<StudentDTO> param, boolean intelligent, List<StudentDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<StudentDTO> studentList;
        if (intelligent) {
            studentList = list;
        } else {
            studentList = param.getExcelLoadService().load(param.getFile(), StudentDTO.class);
        }

        if (CollectionUtils.isEmpty(studentList)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord record = new ImportRecord(param.getImportTypeEnum().getCode());
        record.setFileName(param.getFile().getOriginalFilename());
        record.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        record.setProgress(StudentConstant.PROCESSING);
        record.setTotalNum(studentList.size());
        importRecordMapper.insert(record);

        //2、分批处理数据
        beanValid(param, record, studentList);
        return record;
    }

    @Override
    public void beanValid(ImportParam<StudentDTO> param, ImportRecord record, List<StudentDTO> list) throws Exception {
        List<StudentDTO> okList = new ArrayList<>();
        List<StudentDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<StudentDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<StudentDTO> dupList = map.get(false);
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
        List<StudentDTO> okList2 = map.get(true);
        businessValid(param, okList2, errList, record);
    }

    /**
     * 转换数据,将Excel中的某些数据转换为码表
     *
     * @param student  学生数据
     * @param clazzMap 班级数据
     */
    private void convertData(StudentDTO student, Map<String, String> clazzMap) {
        // 年级和班级
        String gradeAndClazz = student.getGrade() + "###" + student.getClazz();
        if (clazzMap.size() == 0) {
            student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "系统中不存在该年级和班级信息" : student.getErrorMsg() + ";系统中不存在该年级和班级信息");
            student.setSuccess(false);
        } else {
            if (!clazzMap.containsKey(gradeAndClazz)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "年级或班级信息错误" : student.getErrorMsg() + ";年级或班级信息错误");
                student.setSuccess(false);
            }
        }

        // 学籍状态
        StudentStatus studentStatus = null;
        if (StringUtils.isNotBlank(student.getStudentStatus())) {
            studentStatus = StudentStatus.getStudentStatus(student.getStudentStatus());
            if (StudentStatus.NONE.equals(studentStatus)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "学籍状态填写错误" : student.getErrorMsg() + ";学籍状态填写错误");
                student.setSuccess(false);
            }
        }

        // 家长关系
        Relationship relationship = null;
        if (StringUtils.isNotBlank(student.getRelationShip())) {
            relationship = Relationship.getRelationship(student.getRelationShip());
            if (Objects.isNull(relationship)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "家长关系填写错误" : student.getErrorMsg() + ";家长关系填写错误");
                student.setSuccess(false);
            }
        }

        // 政治面貌
        PoliticsStatus politicsStatus = null;
        if (StringUtils.isNotBlank(student.getPoliticsStatus())) {
            politicsStatus = PoliticsStatus.getPoliticsStatus(student.getPoliticsStatus());
            if (Objects.isNull(politicsStatus)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "政治面貌填写错误" : student.getErrorMsg() + ";政治面貌填写错误");
                student.setSuccess(false);
            }
        }

        // 家中排行
        FamilyShort familyShort = null;
        if (StringUtils.isNotBlank(student.getFamilySort())) {
            familyShort = FamilyShort.getFamilyShort(student.getFamilySort());
            if (Objects.isNull(familyShort)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "家中排行填写错误" : student.getErrorMsg() + ";家中排行填写错误");
                student.setSuccess(false);
            }
        }

        // 血型
        BloodType bloodType = null;
        if (StringUtils.isNotBlank(student.getBloodType())) {
            bloodType = BloodType.getBloodType(student.getBloodType());
            if (Objects.isNull(bloodType)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "血型填写错误" : student.getErrorMsg() + ";血型填写错误");
                student.setSuccess(false);
            }
        }

        // 健康状况
        HealthyStatus healthyStatus = null;
        if (StringUtils.isNotBlank(student.getHealthyStatus())) {
            healthyStatus = HealthyStatus.getHealthyStatus(student.getHealthyStatus());
            if (Objects.isNull(healthyStatus)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "健康状况填写错误" : student.getErrorMsg() + ";健康状况填写错误");
                student.setSuccess(false);
            }
        }

        // 是否是留守儿童
        LeftBehindChildren leftBehindChildren = null;
        if (StringUtils.isNotBlank(student.getIsLeft())) {
            leftBehindChildren = LeftBehindChildren.getLeftBehindChildren(student.getIsLeft());
            if (Objects.isNull(leftBehindChildren)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "是否留守儿童填写错误" : student.getErrorMsg() + ";是否留守儿童填写错误");
                student.setSuccess(false);
            }
        }

        // 残疾类型
        DisabilityType disabilityType = null;
        if (StringUtils.isNotBlank(student.getDisabilityType())) {
            disabilityType = DisabilityType.getDisabilityType(student.getDisabilityType());
            if (Objects.isNull(disabilityType)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "残疾类型填写错误" : student.getErrorMsg() + ";残疾类型填写错误");
                student.setSuccess(false);
            }
        }

        // 特殊情况分类
        SpecialCondition specialCondition = null;
        if (StringUtils.isNotBlank(student.getSpecialCondition())) {
            specialCondition = SpecialCondition.getSpecialCondition(student.getSpecialCondition());
            if (Objects.isNull(specialCondition)) {
                student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "特殊情况分类填写错误" : student.getErrorMsg() + ";特殊情况分类填写错误");
                student.setSuccess(false);
            }
        }

        // 家庭情况分类
        List<String> familyConditions = new ArrayList<>();
        String familyConditionStr = student.getFamilyCondition();
        if (StringUtils.isNotBlank(familyConditionStr)) {
            familyConditionStr = familyConditionStr.replace("；", ";");
            String[] data = familyConditionStr.split(";");
            for (String datum : data) {
                FamilyCondition familyCondition = FamilyCondition.getFamilyCondition(datum);
                if (Objects.isNull(familyCondition)) {
                    student.setErrorMsg(StringUtils.isBlank(student.getErrorMsg()) ? "家庭情况分类填写错误" : student.getErrorMsg() + ";家庭情况分类填写错误");
                    student.setSuccess(false);
                    break;
                }
                familyConditions.add(String.valueOf(familyCondition.getCode()));
            }
        }

        if (Boolean.TRUE.equals(student.success())) {
            // 年级和班级
            String value = clazzMap.get(gradeAndClazz);
            String[] data = value.split("###");
            student.setGrade(data[0]);
            student.setClazz(data[1]);

            // 学籍状态
            if (Objects.nonNull(studentStatus)) {
                student.setStudentStatus(String.valueOf(studentStatus.getCode()));
            }

            // 家长关系
            if (Objects.nonNull(relationship)) {
                student.setRelationShip(String.valueOf(relationship.getCode()));
            }

            // 政治面貌
            if (Objects.nonNull(politicsStatus)) {
                student.setPoliticsStatus(String.valueOf(politicsStatus.getCode()));
            }

            // 家长排行
            if (Objects.nonNull(familyShort)) {
                student.setFamilySort(String.valueOf(familyShort.getCode()));
            }

            // 血型
            if (Objects.nonNull(bloodType)) {
                student.setBloodType(String.valueOf(bloodType.getCode()));
            }

            // 健康状况
            if (Objects.nonNull(healthyStatus)) {
                student.setHealthyStatus(String.valueOf(healthyStatus.getCode()));
            }

            // 是否是留守儿童
            if (Objects.nonNull(leftBehindChildren)) {
                student.setIsLeft(String.valueOf(leftBehindChildren.getCode()));
            }

            // 残疾类型
            if (Objects.nonNull(disabilityType)) {
                student.setDisabilityType(String.valueOf(disabilityType.getCode()));
            }

            // 特殊情况分类
            if (Objects.nonNull(specialCondition)) {
                student.setSpecialCondition(String.valueOf(specialCondition.getCode()));
            }

            // 家庭情况分类
            if (CollectionUtils.isNotEmpty(familyConditions)) {
                student.setFamilyCondition(StringUtils.join(familyConditions, ','));
            }
        }
    }

    /**
     * 处理家长数据
     */
    private void handleParent(ImportParam<StudentDTO> param, List<StudentDTO> okList, List<StudentDTO> errList) throws InterruptedException {
        // 校验有家长信息时，家长姓名和关系不能为空
        List<String> parentMobiles = new ArrayList<>();
        List<StudentDTO> tempLists = new ArrayList<>();
        okList.forEach(student -> {
            String parentMobile = student.getParentMobile();
            if (StringUtils.isNotBlank(parentMobile) && (StringUtils.isBlank(student.getParentName()) || StringUtils
                    .isBlank(student.getRelationShip()))) {
                parentMobiles.add(parentMobile);

                if (StringUtils.isEmpty(student.getErrorMsg())) {
                    student.setErrorMsg("家长姓名或家长关系为空");
                } else {
                    student.setErrorMsg(student.getErrorMsg() + ";" + "家长姓名或家长关系为空");
                }
                student.setSuccess(false);

                tempLists.add(student);
            }
        });

        if (CollUtil.isNotEmpty(tempLists)) {
            errList.addAll(tempLists);
            okList.removeAll(tempLists);
        }

        if (CollUtil.isEmpty(parentMobiles)) {
            return;
        }

        List<Parent> parents = new ArrayList<>();
        // 数据行数超过1000,则分批次处理
        int count = parentMobiles.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    parents.addAll(parentMapper.selectByParent(param.getSchoolId(), parentMobiles.subList(i * batchCount, count), key));
                } else {
                    parents.addAll(parentMapper.selectByParent(param.getSchoolId(), parentMobiles.subList(i * batchCount, (i + 1) * batchCount), key));
                }
            }
        } else {
            parents.addAll(parentMapper.selectByParent(param.getSchoolId(), parentMobiles, key));
        }

        if (CollectionUtils.isNotEmpty(parents)) {
            Map<String, Parent> parentData = new HashMap<>(parents.size());
            parents.forEach(parent -> parentData.put(parent.getUsername(), parent));

            okList.forEach(student -> {
                if (StringUtils.isNotBlank(student.getParentName()) && StringUtils.isNotBlank(student.getParentMobile())
                        && StringUtils.isNotBlank(student.getRelationShip()) && parentData
                        .containsKey(student.getParentMobile())) {
                    Parent parent = parentData.get(student.getParentMobile());
                    student.setParentId(parent.getId());
                    student.setParentUserId(parent.getUserId());
                }
            });
        }
    }

    /**
     * 处理学生数据
     *
     * @param param   上传参数
     * @param okList  正常数据
     * @param errList 错误数据
     * @throws Exception 异常信息
     */
    private Map<String, List<StudentDTO>> handleStudent(ImportParam<StudentDTO> param, List<StudentDTO> okList, List<StudentDTO> errList) throws Exception {
        List<StudentDTO> insertData;
        List<StudentDTO> updateData;

        List<String> idCardNumbers = okList.stream().map(StudentDTO::getIdCardNumber).collect(Collectors.toList());
        List<StudentBaseInfo> dupStudentList = getStudent(param.getSchoolId(), idCardNumbers);
        if (CollectionUtils.isNotEmpty(dupStudentList)) {

            Set<String> dupKeys = dupStudentList.stream().map(StudentBaseInfo::getUsername).collect(Collectors.toSet());
            insertData = okList.stream().filter(e -> !dupKeys.contains(e.getIdCardNumber())).collect(Collectors.toList());
            updateData = okList.stream().filter(e -> dupKeys.contains(e.getIdCardNumber())).collect(Collectors.toList());

            //如果不覆盖，直接提示用户数据重复
            if (Boolean.FALSE.equals(param.getCover())) {
                //如果不覆盖，就需要把重复数据加入错误数据集中
                for (StudentDTO staffDTO : updateData) {
                    staffDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                    errList.add(staffDTO);
                }
                updateData.clear();
            } else {
                Map<String, StudentBaseInfo> baseInfoMap =
                    dupStudentList.stream().collect(Collectors.toMap(StudentBaseInfo::getUsername, e -> e));
                //查询出已经存在的Id
                updateData.forEach(e -> {
                    StudentBaseInfo baseInfo = baseInfoMap.get(e.getIdCardNumber());
                    if (baseInfo != null) {
                        if (Objects.nonNull(baseInfo.getId())) {
                            e.setId(baseInfo.getId());
                        }
                        if (Objects.nonNull(baseInfo.getUserId())) {
                            e.setUserId(baseInfo.getUserId());
                        }
                        if (Objects.nonNull(baseInfo.getStudentAdditionalInfoId())) {
                            e.setStudentAdditionalInfoId(baseInfo.getStudentAdditionalInfoId());
                        }
                        if (CollectionUtils.isNotEmpty(baseInfo.getStudentFamilyConditionIds())) {
                            e.setStudentFamilyConditionIds(baseInfo.getStudentFamilyConditionIds());
                        }
                    }
                });
            }
        } else {
            insertData = new ArrayList<>(okList);
            updateData = new ArrayList<>();
        }

        Map<String, List<StudentDTO>> result = new HashMap<>(2);
        result.put("insert", insertData);
        result.put("update", updateData);
        return result;
    }

    @Override
    public void businessValid(ImportParam<StudentDTO> param, List<StudentDTO> okList, List<StudentDTO> errList, ImportRecord record) throws Exception {
        List<StudentDTO> insertData = new ArrayList<>();
        List<StudentDTO> updateData = new ArrayList<>();

        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertData, updateData, errList, record);
            return;
        }

        //处理家长信息
        handleParent(param, okList, errList);

        //处理学生信息
        Map<String, List<StudentDTO>> handleResult = handleStudent(param, okList, errList);
        insertData = handleResult.get("insert");
        updateData = handleResult.get("update");

        // 获取当前学校的年级和班级信息
        Map<String, String> clazzMap = new HashMap<>(16);
        List<ClazzDTO> clazzList;
        String roleCode = param.getRoleCode();
        if (RoleEnum.school_head_teacher.getValue().equals(roleCode)) {
            clazzList = clazzService.getListByHeaderTeacher();
        } else {
            clazzList = clazzService.listAll(param.getSchoolId());
        }

        if (CollectionUtils.isNotEmpty(clazzList)) {
            clazzList.forEach(clazz -> {
                SysDictDTO dictData = RedisUtils.getSingleDictData(clazz.getGrade());
                if (Objects.nonNull(dictData)) {
                    clazzMap.put(dictData.getDictName() + "###" + clazz.getClazzName(), dictData.getDictValue() + "###" + clazz.getId());
                }
            });
        }

        if (CollectionUtils.isNotEmpty(insertData)) {
            // 数据转换
            insertData.forEach(student -> convertData(student, clazzMap));

            // 将转后的数据进行筛选，错误的进入错误列表
            List<StudentDTO> errorList = new ArrayList<>();
            for (StudentDTO studentDTO : insertData) {
                if (Boolean.FALSE.equals(studentDTO.success())) {
                    errorList.add(studentDTO);
                }
            }
            errList.addAll(errorList);
            insertData.removeAll(errorList);
        }

        if (CollectionUtils.isNotEmpty(updateData)) {
            // 数据转换
            updateData.forEach(student -> convertData(student, clazzMap));

            // 将转后的数据进行筛选，错误的进入错误列表
            List<StudentDTO> errorList = new ArrayList<>();
            for (StudentDTO studentDTO : updateData) {
                if (Boolean.FALSE.equals(studentDTO.success())) {
                    errorList.add(studentDTO);
                }
            }
            errList.addAll(errorList);
            updateData.removeAll(errorList);
        }

        updateData(param, insertData, updateData, errList, record);
    }

    @Override
    public void updateData(ImportParam<StudentDTO> resultParams, List<StudentDTO> insertDTOS, List<StudentDTO> updateDTOS, List<StudentDTO> errList, ImportRecord record)
        throws Exception {
        // 更新数据
        if (CollectionUtils.isNotEmpty(updateDTOS)) {
            List<Object> studentUserData = new ArrayList<>();
            List<Object> parentUserData = new ArrayList<>();
            AtomicBoolean insert = new AtomicBoolean(false);

            updateDTOS.forEach(e -> {
                studentUserData.add(e.getSysUser(resultParams));

                SysUser parentUser = e.getParentUser(resultParams);
                if (StringUtils.isNotBlank(parentUser.getUsername())) {
                    if (Objects.isNull(parentUser.getId())) {
                        if (!insert.get()) {
                            insert.set(true);
                        }
                    }
                    parentUserData.add(parentUser);
                }
            });

            UserUtil.saveOrUpdateBatch(studentUserData, sysUserMapper);

            if (CollUtil.isNotEmpty(parentUserData)) {
                UserUtil.saveOrUpdateBatch(parentUserData, sysUserMapper);

                if (insert.get()) {
                    List<SysUser> parentUsers = (List)parentUserData;
                    Map<String, Long> idsMap = parentUsers.stream().collect(Collectors.toMap(SysUser::getUsername, SysUser::getId, (key1, key2) -> key2));

                    updateDTOS.forEach(e -> {
                        Long userId = idsMap.get(e.getParentMobile());
                        if (Objects.nonNull(userId)) {
                            e.setParentUserId(userId);
                        }
                    });
                }
            }

            // 批量保存家长信息
            List<Object> parentData = new ArrayList<>();
            updateDTOS.forEach(e -> {
                Parent parent = e.getParent(resultParams);
                if (StringUtils.isNotBlank(parent.getUsername())) {
                    parentData.add(parent);
                }
            });

            if (CollUtil.isNotEmpty(parentData)) {
                UserUtil.saveOrUpdateBatch(parentData, parentMapper);
            }

            // 批量保存学生信息
            saveBatch(updateDTOS, resultParams);
        }

        // 插入数据
        if (CollectionUtils.isNotEmpty(insertDTOS)) {
            // 批量保存家长信息
            List<Object> parentUserData = new ArrayList<>();
            insertDTOS.forEach(e -> {
                SysUser parentUser = e.getParentUser(resultParams);
                if (StringUtils.isNotBlank(parentUser.getUsername())) {
                    parentUserData.add(parentUser);
                }
            });

            List<SysUser> parentUsers;
            if (CollectionUtils.isNotEmpty(parentUserData)) {
                // 批量保存家长用户信息
                UserUtil.saveOrUpdateBatch(parentUserData, sysUserMapper);

                parentUsers = (List)parentUserData;
                Map<String, Long> idsMap = parentUsers.stream().collect(Collectors.toMap(SysUser::getUsername, SysUser::getId, (key1, key2) -> key2));

                insertDTOS.forEach(e -> {
                    Long userId = idsMap.get(e.getParentMobile());
                    if (Objects.nonNull(userId)) {
                        e.setParentUserId(userId);
                    }
                });

                // 批量保存家长信息
                List<Object> parentData = new ArrayList<>();
                insertDTOS.forEach(e -> {
                    Parent parent = e.getParent(resultParams);
                    if (StringUtils.isNotBlank(parent.getUsername())) {
                        parentData.add(parent);
                    }
                });
                UserUtil.saveOrUpdateBatch(parentData, parentMapper);

                // 设置家长id
                List<Parent> parentList = (List)parentData;
                // 转换成Map
                Map<String, Long> usernameAndParentIdMap = parentList.stream().collect(Collectors.toMap(Parent::getUsername, Parent::getId, (key1, key2) -> key2));
                insertDTOS.forEach(e -> {
                    Long parentId = usernameAndParentIdMap.get(e.getParentMobile());
                    if (Objects.nonNull(parentId)) {
                        e.setParentId(parentId);
                    }
                });
            }

            // 批量保存学生信息
            List<Object> studentData = new ArrayList<>();
            insertDTOS.forEach(e -> {
                studentData.add(e.getSysUser(resultParams));
            });



            // 批量保存学生用户信息
            UserUtil.saveOrUpdateBatch(studentData, sysUserMapper);

            List<SysUser> studentUsers = (List)studentData;
            Map<String, Long> idsMap = studentUsers.stream().collect(Collectors.toMap(SysUser::getUsername, SysUser::getId, (key1, key2) -> key2));

            //遍历,把保存后的用户userId取过来做关联
            insertDTOS.forEach(e -> {
                Long userId = idsMap.get(e.getIdCardNumber());
                if (Objects.nonNull(userId)) {
                    e.setUserId(userId);
                }
            });

            // 批量保存学生信息
            saveBatch(insertDTOS, resultParams);
        }

        int successNum = 0;
        if (CollectionUtils.isNotEmpty(insertDTOS)) {
            successNum += insertDTOS.size();
        }
        if (CollectionUtils.isNotEmpty(updateDTOS)) {
            successNum += updateDTOS.size();
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

    @Override
    public void saveBatch(List<StudentDTO> list, ImportParam<StudentDTO> importParam) throws Exception {
        // 批量保存学生用户信息
        List<Object> studentBaseInfoData = new ArrayList<>();
        list.forEach(e -> studentBaseInfoData.add(e.getStudentBaseInfo(importParam)));
        UserUtil.saveOrUpdateBatch(studentBaseInfoData, studentBaseInfoMapper);

        List<StudentBaseInfo> studentBaseInfos = (List)studentBaseInfoData;
        // 转换成Map
        Map<String, Long> idsMap = studentBaseInfos.stream().collect(Collectors.toMap(StudentBaseInfo::getIdentityCardNumber, StudentBaseInfo::getId, (key1, key2) -> key2));

        // 批量保存学生补充信息
        List<Object> studentAdditionalInfoData = new ArrayList<>();
        list.forEach(e -> {
            Long studentId = idsMap.get(e.getIdCardNumber());
            if (Objects.nonNull(studentId)) {
                e.setStudentId(studentId);
                studentAdditionalInfoData.add(e.getStudentAdditionalInfo(importParam));
            }
        });
        UserUtil.saveOrUpdateBatch(studentAdditionalInfoData, studentAdditionalInfoMapper);

        // 批量保存学生家庭情况
        List<Object> studentFamilyConditions = new ArrayList<>();
        list.forEach(e -> {
            Long studentId = idsMap.get(e.getIdCardNumber());
            if (Objects.nonNull(studentId)) {
                e.setStudentId(studentId);
                studentFamilyConditions.addAll(e.getStudentFamilyConditions(importParam));
            }
        });
        UserUtil.saveOrUpdateBatch(studentFamilyConditions, studentFamilyConditionMapper);
    }

    @Override
    public String errorWriteToExcel(IFileService<StudentDTO> fileService, String fileName, List<StudentDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, StudentDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    public List<StudentBaseInfo> getStudent(Long schoolId, List<String> idCardNumbers) throws Exception {
        List<StudentBaseInfo> studentList = new ArrayList<>();

        // 数据行数超过100,则分批次处理
        int count = idCardNumbers.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            List<String> idCards;
            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    idCards = idCardNumbers.subList(i * batchCount, count);
                } else {
                    idCards = idCardNumbers.subList(i * batchCount, (i + 1) * batchCount);
                }
                studentList.addAll(studentBaseInfoMapper.selectByStudent(schoolId, idCards, key));
            }
        } else {
            studentList.addAll(studentBaseInfoMapper.selectByStudent(schoolId, idCardNumbers, key));
        }

        return studentList;
    }

    @Override
    public List<SysUser> unique(List<String> idCardNumbers) {
        return Collections.emptyList();
    }

    @Override
    public Map<Boolean, List<StudentDTO>> duplicate(List<StudentDTO> sourceList) {
        Map<Boolean, List<StudentDTO>> resultMap = new HashMap<>(16);
        List<StudentDTO> okList = new ArrayList<>();
        List<StudentDTO> errList = new ArrayList<>();

        Map<String, List<StudentDTO>> map = sourceList.stream().collect(Collectors.groupingBy(StudentDTO::getIdCardNumber));

        map.forEach((idCardNumber, studentDTOS) -> {
            if (studentDTOS.size() > 1) {
                okList.add(studentDTOS.get(0));
                errList.addAll(studentDTOS.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(studentDTOS);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }
}
