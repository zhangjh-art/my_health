package com.cnasoft.health.userservice.excel.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.AreaStaffType;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.Sex;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.MyRegularExpression;
import com.cnasoft.health.userservice.excel.encrypt.EncryptField;
import com.cnasoft.health.userservice.excel.validator.IValidationErrorResult;
import com.cnasoft.health.userservice.excel.validator.ListRange;
import com.cnasoft.health.userservice.excel.validator.ValidationErrorResult;
import com.cnasoft.health.userservice.model.AreaStaff;
import com.cnasoft.health.userservice.model.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@ExcelIgnoreUnannotated
public class AreaStaffDTO extends ValidationErrorResult implements IValidationErrorResult {

    public SysUser getSysUser(ImportParam<AreaStaffDTO> param) {
        SysUser user = new SysUser();
        user.setId(this.getUserId());
        user.setEmail(this.getEmail());
        user.setMobile(this.mobile);
        user.setEnabled(true);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());
        user.setAreaCode(param.getAreaCode());
        user.setName(this.name);
        user.setPassword(this.getPassword());
        AreaStaffType staffType = AreaStaffType.getStaffType(this.getType());
        if (Objects.nonNull(staffType)) {
            if (staffType.equals(AreaStaffType.LEADER)) {
                user.setRoleCode(RoleEnum.region_leader.getValue());
            } else {
                user.setRoleCode(RoleEnum.region_staff.getValue());
            }
        } else {
            user.setRoleCode(RoleEnum.region_staff.getValue());
        }

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
     * 返回区域职员信息
     *
     * @param param 参数
     * @return 区域职员
     */
    public AreaStaff getSelf(ImportParam<AreaStaffDTO> param) {
        AreaStaff staff = new AreaStaff();
        staff.setId(this.getId());
        staff.setUserId(this.getUserId());
        staff.setDepartment(this.getDepartment());
        staff.setAreaCode(param.getAreaCode());
        staff.setJobNumber(this.getJobNumber());
        staff.setPost(this.getPost());

        AreaStaffType staffType = AreaStaffType.getStaffType(this.getType());
        if (ObjectUtils.isNotEmpty(staffType)) {
            staff.setType(staffType.getCode());
        } else {
            staff.setType(AreaStaffType.NORMAL_STAFF.getCode());
        }
        if (Objects.isNull(staff.getId())) {
            staff.setCreateBy(param.getCreateBy());
            staff.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(staff.getId())) {
            staff.setUpdateBy(param.getUpdateBy());
            staff.setUpdateTime(param.getDateInterface().now());
        }
        staff.setIsDeleted(false);
        return staff;
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
     * 部门
     */
    @ExcelProperty(index = 3, value = "部门*")
    private String department;

    /**
     * 岗位
     */
    @ExcelProperty(index = 4, value = "岗位*")
    private String post;

    /**
     * 工号
     */
    @ExcelProperty(index = 5, value = "工号*")
    private String jobNumber;

    /**
     * 职工类型
     */
    @ExcelProperty(index = 6, value = "职员类型*")
    @ListRange(value = {"普通职员", "领导"}, message = "职员类型错误")
    @NotNull(message = "职员类型不能为空")
    private String type;

    /**
     * 电子邮箱
     */
    @ExcelProperty(index = 7, value = "邮箱*")
    @Pattern(regexp = MyRegularExpression.EMAIL, message = "电子邮箱格式不正确")
    private String email;

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
     * 构造密码
     *
     * @return 区域职员
     */
    public AreaStaffDTO password() {
        setPassword(this.mobile.substring(5, 11));
        return this;
    }

    /**
     * 姓名（必填）   性别（必填）      手机号（必填）	部门（非必填）
     * 岗位（非必填）  工号（非必填） 职员类型（必填）    邮箱（非必填）
     */
    @Data
    @HeadRowHeight(value = 30)
    @ExcelIgnoreUnannotated
    @ContentRowHeight(20)
    public static class ExcelHead {
        final static int MIN_WIDTH = 18;

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
         * 部门 数据字典值
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 3, value = "部门（非必填）")
        private String department;

        /**
         * 岗位
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 4, value = "岗位（非必填）")
        private String post;

        /**
         * 工号
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 5, value = "工号（非必填）")
        private String jobNumber;

        /**
         * 职员类型
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 6, value = "职员类型（必填）")
        private String type;

        /**
         * 电子邮箱
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 7, value = "邮箱（非必填）")
        private String email;
        /**
         * 错误提示
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 8, value = "错误提示")
        private String errorMsg;
    }
}
