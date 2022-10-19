package com.cnasoft.health.userservice.excel.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.cnasoft.health.common.encryptor.EncryptorUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.StudentStatus;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.validator.IValidationErrorResult;
import com.cnasoft.health.userservice.excel.validator.ListRange;
import com.cnasoft.health.userservice.excel.validator.ValidationErrorResult;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.StudentAdditionalInfo;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.StudentFamilyCondition;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ganghe
 * @date 2022/5/1 16:36
 **/
@Data
@ExcelIgnoreUnannotated
@EqualsAndHashCode(callSuper = true)
public class StudentDTO extends ValidationErrorResult implements IValidationErrorResult {

    public SysUser getSysUser(ImportParam<StudentDTO> param) {
        SysUser user = new SysUser();
        user.setId(this.getUserId());
        user.setUsername(this.idCardNumber);
        user.setPassword(this.getPassword());
        user.setName(this.name);
        user.setSex(UserUtil.getSex(this.idCardNumber));
        user.setMobile(this.studentMobile);
        user.setEmail(this.email);
        user.setEnabled(true);
        user.setSchoolId(param.getSchoolId());
        user.setRoleCode(RoleEnum.student.getValue());
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());

        if (Objects.isNull(user.getId())) {
            user.setCreateBy(param.getCreateBy());
            user.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(user.getId())) {
            user.setUpdateBy(param.getUpdateBy());
            user.setUpdateTime(param.getDateInterface().now());
        }
        user.setIsDeleted(false);
        return user;
    }

    public SysUser getParentUser(ImportParam<StudentDTO> param) {
        SysUser user = new SysUser();
        user.setId(this.getParentUserId());
        user.setUsername(this.parentMobile);
        user.setPassword(this.getParentPassword());
        user.setName(this.parentName);
        user.setMobile(this.parentMobile);
        user.setEnabled(true);
        user.setSchoolId(param.getSchoolId());
        user.setRoleCode(RoleEnum.parents.getValue());
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());

        if (Objects.isNull(user.getId())) {
            user.setCreateBy(param.getCreateBy());
            user.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(user.getId())) {
            user.setUpdateBy(param.getUpdateBy());
            user.setUpdateTime(param.getDateInterface().now());
        }
        user.setIsDeleted(false);
        return user;
    }

    /**
     * 返回学生基本信息
     *
     * @param param 数据列表
     * @return 学生基本信息
     */
    public StudentBaseInfo getStudentBaseInfo(ImportParam<StudentDTO> param) {
        StudentBaseInfo student = new StudentBaseInfo();
        student.setId(this.getId());
        student.setUserId(this.getUserId());
        student.setUsername(this.idCardNumber);
        student.setName(this.name);
        student.setSex(UserUtil.getSex(this.idCardNumber));
        student.setMobile(this.studentMobile);
        student.setEmail(this.email);
        student.setEnabled(true);
        student.setSchoolId(param.getSchoolId());
        student.setGrade(this.grade);
        student.setClazzId(Long.parseLong(this.getClazz()));
        student.setIdentityCardNumber(this.idCardNumber);
        student.setBirthday(UserUtil.getBirthday(this.idCardNumber));
        student.setStudentNumber(this.studentNumber);
        student.setStudentCode(this.studentCode);
        student.setStudentStatus(StringUtils.isNotBlank(this.studentStatus) ? Integer.parseInt(this.studentStatus) : StudentStatus.NORMAL.getCode());
        student.setParentId(this.getParentId());
        student.setRelationship(StringUtils.isNotBlank(this.relationShip) ? Integer.parseInt(this.relationShip) : null);
        //根据年级设置入学年份
        student.setAdmissionYear(UserUtil.getAdmissionYear(this.grade));

        if (Objects.isNull(student.getId())) {
            student.setCreateBy(param.getCreateBy());
            student.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(student.getId())) {
            student.setUpdateBy(param.getUpdateBy());
            student.setUpdateTime(param.getDateInterface().now());
        }
        student.setIsDeleted(false);
        return student;
    }

    /**
     * 返回学生补充信息
     *
     * @param param 数据列表
     * @return 学生补充信息
     */
    public StudentAdditionalInfo getStudentAdditionalInfo(ImportParam<StudentDTO> param) {
        StudentAdditionalInfo studentAdditionalInfo = new StudentAdditionalInfo();
        studentAdditionalInfo.setId(this.getStudentAdditionalInfoId());
        studentAdditionalInfo.setStudentId(this.getStudentId());
        studentAdditionalInfo.setCountry("中国".equals(this.country) ? 1 : 0);
        studentAdditionalInfo.setNation(this.nation);
        studentAdditionalInfo.setGatqw(this.isWhether(this.gatqw));
        studentAdditionalInfo.setPoliticsStatus(StringUtils.isNotBlank(this.politicsStatus) ? Integer.parseInt(this.politicsStatus) : null);
        studentAdditionalInfo.setNativePlace(this.nativePlace);
        studentAdditionalInfo.setBirthPlace(this.birthPlace);
        studentAdditionalInfo.setFamilySort(StringUtils.isNotBlank(this.familySort) ? Integer.parseInt(this.familySort) : null);
        studentAdditionalInfo.setBloodType(StringUtils.isNotBlank(this.bloodType) ? Integer.parseInt(this.bloodType) : null);
        studentAdditionalInfo.setHealthyStatus(StringUtils.isNotBlank(this.healthyStatus) ? Integer.parseInt(this.healthyStatus) : null);
        studentAdditionalInfo.setIllnessHistory(this.illnessHistory);
        studentAdditionalInfo.setIsOrphan(this.isWhether(this.isOrphan));
        studentAdditionalInfo.setIsLeft(StringUtils.isNotBlank(this.isLeft) ? Integer.parseInt(this.isLeft) : null);
        studentAdditionalInfo.setIsOnly(this.isWhether(this.isOnly));
        studentAdditionalInfo.setDisabilityType(StringUtils.isNotBlank(this.disabilityType) ? Integer.parseInt(this.disabilityType) : null);
        studentAdditionalInfo.setIsChildOfMartyrEntitled(this.isWhether(this.isChildOfMartyrEntitled));
        studentAdditionalInfo.setIsChildOfMigrant(this.isWhether(this.isChildOfMigrant));
        studentAdditionalInfo.setIsSubsidized(this.isWhether(this.isSubsidized));
        studentAdditionalInfo.setSpecialCondition(StringUtils.isNotBlank(this.specialCondition) ? Integer.parseInt(this.specialCondition) : null);
        studentAdditionalInfo.setHeight(this.height);
        studentAdditionalInfo.setWeight(this.weight);

        if (Objects.isNull(studentAdditionalInfo.getId())) {
            studentAdditionalInfo.setCreateBy(param.getCreateBy());
            studentAdditionalInfo.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(studentAdditionalInfo.getId())) {
            studentAdditionalInfo.setUpdateBy(param.getUpdateBy());
            studentAdditionalInfo.setUpdateTime(param.getDateInterface().now());
        }
        studentAdditionalInfo.setIsDeleted(false);
        return studentAdditionalInfo;
    }

    /**
     * 返回家长信息
     *
     * @param param 数据列表
     * @return 学生补充信息
     */
    public Parent getParent(ImportParam<StudentDTO> param) {
        Parent parent = new Parent();
        parent.setId(this.getParentId());
        parent.setUserId(this.getParentUserId());
        parent.setUsername(this.parentMobile);
        parent.setName(this.parentName);
        parent.setMobile(this.parentMobile);
        parent.setSchoolId(param.getSchoolId());
        parent.setEnabled(true);
        parent.setIsActive(false);
        parent.setConfirmed(false);

        if (Objects.isNull(parent.getId())) {
            parent.setCreateBy(param.getCreateBy());
            parent.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(parent.getId())) {
            parent.setUpdateBy(param.getUpdateBy());
            parent.setUpdateTime(param.getDateInterface().now());
        }
        parent.setIsDeleted(false);
        return parent;
    }

    /**
     * 返回学生补充信息
     *
     * @param param 数据列表
     * @return 学生补充信息
     */
    public List<StudentFamilyCondition> getStudentFamilyConditions(ImportParam<StudentDTO> param) {
        List<StudentFamilyCondition> studentFamilyConditions = new ArrayList<>();
        if (StringUtils.isNotBlank(this.familyCondition)) {
            List<Long> studentFamilyConditionIds = this.getStudentFamilyConditionIds();
            String[] familyConditions = this.familyCondition.split(",");

            if (ArrayUtils.isNotEmpty(familyConditions)) {
                for (int i = 0; i < familyConditions.length; i++) {
                    StudentFamilyCondition studentFamilyCondition = new StudentFamilyCondition();

                    if (CollectionUtils.isNotEmpty(studentFamilyConditionIds)) {
                        studentFamilyCondition.setId(studentFamilyConditionIds.get(i));
                    } else {
                        studentFamilyCondition.setId(null);
                    }
                    studentFamilyCondition.setStudentId(this.getStudentId());
                    studentFamilyCondition.setFamilyCondition(Integer.valueOf(familyConditions[i]));
                    if (Objects.isNull(studentFamilyCondition.getId())) {
                        studentFamilyCondition.setCreateBy(param.getCreateBy());
                        studentFamilyCondition.setCreateTime(param.getDateInterface().now());
                    }
                    if (Objects.nonNull(studentFamilyCondition.getId())) {
                        studentFamilyCondition.setUpdateBy(param.getUpdateBy());
                        studentFamilyCondition.setUpdateTime(param.getDateInterface().now());
                    }
                    studentFamilyCondition.setIsDeleted(false);
                    studentFamilyConditions.add(studentFamilyCondition);
                }
            }
        }

        return studentFamilyConditions;
    }

    /**
     * 姓名
     */
    @ExcelProperty(index = 0, value = "姓名*")
    @NotNull(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度应该在2个汉字到20个汉字之间")
    private String name;

    /**
     * 身份证号
     */
    @ExcelProperty(index = 1, value = "身份证号*")
    @NotNull(message = "身份证号不能为空")
    @Pattern(regexp = TextValidator.REGEX_ID, message = "身份证号格式不正确")
    private String idCardNumber;

    /**
     * 年级
     */
    @ExcelProperty(index = 2, value = "年级*")
    @NotNull(message = "年级不能为空")
    private String grade;

    /**
     * 班级
     */
    @ExcelProperty(index = 3, value = "班级*")
    @NotNull(message = "班级不能为空")
    private String clazz;

    /**
     * 学号
     */
    @ExcelProperty(index = 4, value = "学号*")
    private String studentNumber;

    /**
     * 学籍号
     */
    @ExcelProperty(index = 5, value = "学籍号*")
    private String studentCode;

    /**
     * 学籍状态
     */
    @ExcelProperty(index = 6, value = "学籍状态*")
    @NotNull(message = "学籍状态不能为空")
    private String studentStatus;

    /**
     * 家长姓名
     */
    @ExcelProperty(index = 7, value = "家长姓名*")
    private String parentName;

    /**
     * 家长手机号
     */
    @ExcelProperty(index = 8, value = "家长手机*")
    @Pattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "家长手机号格式不正确")
    private String parentMobile;

    /**
     * 家长关系
     */
    @ExcelProperty(index = 9, value = "家长关系*")
    private String relationShip;

    /**
     * 学生手机号
     */
    @ExcelProperty(index = 10, value = "学生手机*")
    @Pattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "学生手机号格式不正确")
    private String studentMobile;

    /**
     * 电子邮箱
     */
    @ExcelProperty(index = 11, value = "电子邮箱*")
    @Pattern(regexp = TextValidator.REGEX_EMAIL, message = "电子邮箱格式不正确")
    private String email;

    /**
     * 国籍
     */
    @ExcelProperty(index = 12, value = "国籍*")
    private String country;

    /**
     * 港澳台侨外
     */
    @ExcelProperty(index = 13, value = "港澳台侨外*")
    @ListRange(value = {"是", "否"}, message = "港澳台侨外[是,否]之间")
    private String gatqw;

    /**
     * 民族
     */
    @ExcelProperty(index = 14, value = "民族*")
    private String nation;

    /**
     * 政治面貌
     */
    @ExcelProperty(index = 15, value = "政治面貌*")
    private String politicsStatus;

    /**
     * 籍贯
     */
    @ExcelProperty(index = 16, value = "籍贯*")
    private String nativePlace;

    /**
     * 出生地
     */
    @ExcelProperty(index = 17, value = "出生地*")
    private String birthPlace;

    /**
     * 家中排行
     */
    @ExcelProperty(index = 18, value = "家中排行*")
    private String familySort;

    /**
     * 血型
     */
    @ExcelProperty(index = 19, value = "血型*")
    private String bloodType;

    /**
     * 健康状况
     */
    @ExcelProperty(index = 20, value = "健康状况*")
    private String healthyStatus;

    /**
     * 疾病史
     */
    @ExcelProperty(index = 21, value = "疾病史*")
    private String illnessHistory;

    /**
     * 是否是孤儿
     */
    @ExcelProperty(index = 22, value = "是否是孤儿*")
    @ListRange(value = {"是", "否"}, message = "是否是孤儿选项必须在[是,否]之间")
    private String isOrphan;

    /**
     * 是否是留守儿童
     */
    @ExcelProperty(index = 23, value = "是否是留守儿童*")
    private String isLeft;

    /**
     * 是否是独生子女
     */
    @ExcelProperty(index = 24, value = "是否是独生子女*")
    @ListRange(value = {"是", "否"}, message = "是否是独生子女选项必须在[是,否]之间")
    private String isOnly;

    /**
     * 残疾类型
     */
    @ExcelProperty(index = 25, value = "残疾类型*")
    private String disabilityType;

    /**
     * 是否是烈士或优抚子女
     */
    @ExcelProperty(index = 26, value = "是否是烈士或优抚子女*")
    @ListRange(value = {"是", "否"}, message = "是否是烈士或优抚子女选项必须在[是,否]之间")
    private String isChildOfMartyrEntitled;

    /**
     * 是否是进城务工随迁
     */
    @ExcelProperty(index = 27, value = "是否是进城务工随迁*")
    @ListRange(value = {"是", "否"}, message = "是否是进城务工随迁选项必须在[是,否]之间")
    private String isChildOfMigrant;

    /**
     * 是否申请资助
     */
    @ExcelProperty(index = 28, value = "是否申请资助*")
    @ListRange(value = {"是", "否"}, message = "是否申请资助选项必须在[是,否]之间")
    private String isSubsidized;

    /**
     * 身高
     */
    @ExcelProperty(index = 29, value = "身高*")
    private String height;

    /**
     * 体重
     */
    @ExcelProperty(index = 30, value = "体重*")
    private String weight;

    /**
     * 特殊情况分类
     */
    @ExcelProperty(index = 31, value = "特殊情况分类*")
    private String specialCondition;

    /**
     * 家庭情况分类
     */
    @ExcelProperty(index = 32, value = "家庭情况分类*")
    private String familyCondition;

    /**
     * 构造密码
     *
     * @return 学生
     */
    public String getPassword() {
        return EncryptorUtil.encrypt(this.idCardNumber.substring(this.idCardNumber.length() - 6));
    }

    public String getParentPassword() {
        if (StringUtils.isNotBlank(this.parentMobile)) {
            return EncryptorUtil.encrypt(this.parentMobile.substring(this.parentMobile.length() - 6));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void errorMsg(String message) {
        setErrorMsg(message);
    }

    @Override
    public String errorMsg() {
        return getErrorMsg();
    }

    @Override
    public void success(Boolean b) {
        setSuccess(b);
    }

    @Override
    public Boolean success() {
        return Boolean.TRUE.equals(getSuccess());
    }

    @Data
    @HeadRowHeight(value = 30)
    @ExcelIgnoreUnannotated
    @ContentRowHeight(20)
    public static class ExcelHead {
        static final int MIN_WIDTH = 18;

        /**
         * 姓名
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 0, value = "姓名（必填）")
        private String name;

        /**
         * 身份证号
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 1, value = "身份证号（必填）")
        private String idCardNumber;

        /**
         * 年级
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 2, value = "年级（必填）")
        private String grade;

        /**
         * 班级
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 3, value = "班级（必填）")
        private String clazz;

        /**
         * 学号
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 4, value = "学号（非必填）")
        private String studentNumber;

        /**
         * 学籍号
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 5, value = "学籍号（非必填）")
        private String studentCode;

        /**
         * 学籍状态
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 6, value = "学籍状态（必填）")
        private String studentStatus;

        /**
         * 家长姓名
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 7, value = "家长姓名（非必填）")
        private String parentName;

        /**
         * 家长手机号
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 8, value = "家长手机（非必填）")
        private String parentMobile;

        /**
         * 家长关系
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 9, value = "家长关系（非必填）")
        private String relationShip;

        /**
         * 学生手机号
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 10, value = "学生手机（非必填）")
        private String studentMobile;

        /**
         * 电子邮箱
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 11, value = "电子邮箱（非必填）")
        private String email;

        /**
         * 国籍
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 12, value = "国籍（非必填）")
        private String country;

        /**
         * 港澳台侨外
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 13, value = "港澳台侨外（非必填）")
        private String gatqw;

        /**
         * 民族
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 14, value = "民族（非必填）")
        private String nation;

        /**
         * 政治面貌
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 15, value = "政治面貌（非必填）")
        private String politicsStatus;

        /**
         * 籍贯
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 16, value = "籍贯（非必填）")
        private String nativePlace;

        /**
         * 出生地
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 17, value = "出生地（非必填）")
        private String birthPlace;

        /**
         * 家中排行
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 18, value = "家中排行（非必填）")
        private String familySort;

        /**
         * 血型
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 19, value = "血型（非必填）")
        private String bloodType;

        /**
         * 健康状况
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 20, value = "健康状况（非必填）")
        private String healthyStatus;

        /**
         * 疾病史
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 21, value = "疾病史（非必填）")
        private String illnessHistory;

        /**
         * 是否是孤儿
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 22, value = "是否是孤儿（非必填）")
        private String isOrphan;

        /**
         * 是否是留守儿童
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 23, value = "是否是留守儿童（非必填）")
        private String isLeft;

        /**
         * 是否是独生子女
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 24, value = "是否是独生子女（非必填）")
        private String isOnly;

        /**
         * 残疾类型
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 25, value = "残疾类型（非必填）")
        private String disabilityType;

        /**
         * 是否是烈士或优抚子女
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 26, value = "是否是烈士或优抚子女（非必填）")
        private String isChildOfMartyrEntitled;

        /**
         * 是否是进城务工随迁
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 27, value = "是否是进城务工随迁（非必填）")
        private String isChildOfMigrant;

        /**
         * 是否申请资助
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 28, value = "是否申请资助（非必填）")
        private String isSubsidized;

        /**
         * 身高
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 29, value = "身高（非必填）")
        private String height;

        /**
         * 体重
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 30, value = "体重（非必填）")
        private String weight;

        /**
         * 特殊情况分类
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 31, value = "特殊情况分类（非必填）")
        private String specialCondition;

        /**
         * 家庭情况分类
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 32, value = "家庭情况分类（非必填）")
        private String familyCondition;

        /**
         * 错误提示
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 33, value = "错误提示")
        private String errorMsg;
    }
}