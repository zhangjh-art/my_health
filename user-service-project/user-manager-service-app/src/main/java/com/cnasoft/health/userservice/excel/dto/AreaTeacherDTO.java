package com.cnasoft.health.userservice.excel.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.Sex;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.MyRegularExpression;
import com.cnasoft.health.userservice.excel.encrypt.EncryptField;
import com.cnasoft.health.userservice.excel.validator.IValidationErrorResult;
import com.cnasoft.health.userservice.excel.validator.ListRange;
import com.cnasoft.health.userservice.excel.validator.ValidationErrorResult;
import com.cnasoft.health.userservice.model.AreaTeacher;
import com.cnasoft.health.userservice.model.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * 姓名（必填）	性别（必填）	手机号（必填）
 * 是否承接任务（必填）	工号（非必填）	部门（非必填）
 * 岗位（非必填）	职称（非必填） 专业（非必填）	擅长（非必填）	邮箱（非必填）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ExcelIgnoreUnannotated
public class AreaTeacherDTO extends ValidationErrorResult implements IValidationErrorResult {

    public SysUser getSysUser(ImportParam<AreaTeacherDTO> param) {
        SysUser user = new SysUser();
        user.setId(this.getUserId());
        user.setEmail(this.getEmail());
        user.setMobile(this.mobile);
        user.setEnabled(true);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());
        user.setAreaCode(param.getAreaCode());
        user.setName(this.name);
        user.setPassword(this.getPassword());
        user.setRoleCode(RoleEnum.region_psycho_teacher.getValue());
        if (Objects.isNull(user.getId())) {
            user.setCreateBy(param.getCreateBy());
            user.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(user.getId())) {
            user.setUpdateBy(param.getUpdateBy());
            user.setUpdateTime(param.getDateInterface().now());
        }
        user.setIsDeleted(false);
        user.setSex(Sex.getSex(this.getSex()).getCode());
        user.setUsername(this.getMobile());
        return user;
    }

    /**
     * 返回区域心理教研员信息
     *
     * @param param 参数
     * @return 区域心理教研员
     */
    public AreaTeacher getSelf(ImportParam<AreaTeacherDTO> param) {
        AreaTeacher teacher = new AreaTeacher();
        teacher.setId(this.getId());
        teacher.setUserId(this.getUserId());
        teacher.setTitle(this.getTitle());
        teacher.setDepartment(this.getDepartment());
        teacher.setPost(this.getPost());
        teacher.setSpecialty(this.getSpecialty());
        teacher.setIsAcceptTask(this.isWhether(this.getIsAcceptTask()));
        teacher.setMajor(this.getMajor());
        teacher.setAreaCode(param.getAreaCode());
        teacher.setJobNumber(this.getJobNumber());
        teacher.setMajor(this.getMajor());
        if (Objects.isNull(teacher.getId())) {
            teacher.setCreateBy(param.getCreateBy());
            teacher.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(teacher.getId())) {
            teacher.setUpdateBy(param.getUpdateBy());
            teacher.setUpdateTime(param.getDateInterface().now());
        }
        teacher.setIsDeleted(false);
        return teacher;
    }

    /**
     * 姓名
     */
    @ExcelProperty(index = 0, value = "姓名*")
    @NotNull(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度应该在2个汉字到20个汉字之间")
    private String name;

    /**
     * 性别: 1: 男，2: 女
     */
    @ExcelProperty(index = 1, value = "性别*")
    @NotNull(message = "性别不能为空")
    @ListRange(value = {"男", "女"}, message = "性别必须在[男,女]之间")
    private String sex;

    /**
     * 手机号
     */
    @ExcelProperty(index = 2, value = "手机号*")
    @NotNull(message = "手机号不能为空")
    @Pattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式不正确")
    private String mobile;

    /**
     * 是否承接任务
     */
    @ExcelProperty(index = 3, value = "是否承接任务*")
    @NotNull(message = "是否承接任务不能为空")
    @ListRange(value = {"是", "否"}, message = "是否承接任务选项必须在[是,否]之间")
    private String isAcceptTask;

    /**
     * 工号
     */
    @ExcelProperty(index = 4, value = "工号*")
    private String jobNumber;

    /**
     * 部门
     */
    @ExcelProperty(index = 5, value = "部门*")
    private String department;

    /**
     * 岗位
     */
    @ExcelProperty(index = 6, value = "岗位")
    private String post;

    /**
     * 职称
     */
    @ExcelProperty(index = 7, value = "职称*")
    private String title;

    /**
     * 专业
     */
    @ExcelProperty(index = 8, value = "专业*")
    private String major;

    /**
     * 擅长
     */
    @ExcelProperty(index = 9, value = "擅长*")
    private String specialty;

    /**
     * 电子邮箱
     */
    @ExcelProperty(index = 10, value = "邮箱*")
    @Pattern(regexp = MyRegularExpression.EMAIL, message = "电子邮箱格式不正确")
    private String email;

    /**
     * 构造密码
     *
     * @return 区域心理教研员
     */
    public AreaTeacherDTO password() {
        setPassword(this.mobile.substring(5, 11));
        return this;
    }

    /**
     * 密码
     */
    @EncryptField
    private String password;

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
        return getSuccess();
    }

    /**
     * * 姓名（必填）	性别（必填）	手机号（必填）
     * * 是否承接任务（必填）	工号（非必填）	部门（非必填）
     * * 岗位（非必填）    职称（非必填）	专业（非必填）	擅长（非必填）	邮箱（非必填）
     */
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
         * 性别: 1: 男，2: 女
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 1, value = "性别（必填）")
        private String sex;

        /**
         * 手机号
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 2, value = "手机号（必填）")
        private String mobile;

        /**
         * 是否承接任务
         */
        @ColumnWidth(value = MIN_WIDTH + 15)
        @ExcelProperty(index = 3, value = "是否承接任务（必填）")
        private String isAcceptTask;

        /**
         * 工号
         */
        @ExcelProperty(index = 4, value = "工号（非必填）")
        private String jobNumber;

        /**
         * 部门
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 5, value = "部门（非必填）")
        private String department;

        /**
         * 岗位
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 6, value = "岗位（非必填）")
        private String post;

        /**
         * 职称
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 7, value = "职称（非必填）")
        private String title;

        /**
         * 专业
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 8, value = "专业（非必填）")
        private String major;

        /**
         * 擅长
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 9, value = "擅长（非必填）")
        private String specialty;

        /**
         * 电子邮箱
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 10, value = "邮箱（非必填）")
        private String email;

        /**
         * 错误提示
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 11, value = "错误提示")
        private String errorMsg;
    }
}
