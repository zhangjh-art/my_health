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
import com.cnasoft.health.userservice.excel.encrypt.EncryptField;
import com.cnasoft.health.userservice.excel.validator.IValidationErrorResult;
import com.cnasoft.health.userservice.excel.validator.ListRange;
import com.cnasoft.health.userservice.excel.validator.ValidationErrorResult;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.SysUser;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

@Data
@ExcelIgnoreUnannotated
public class ParentDTO extends ValidationErrorResult implements IValidationErrorResult {

    /**
     * 返回用户信息
     *
     * @param param
     * @return
     */
    public SysUser getSysUser(ImportParam<ParentDTO> param) {
        SysUser user = new SysUser();
        user.setId(this.getUserId());
        user.setUsername(this.mobile);
        user.setName(this.name);
        user.setSex(Sex.getSex(this.getSex()).getCode());
        user.setMobile(this.mobile);
        user.setSchoolId(param.getSchoolId());
        user.setEnabled(true);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());
        user.setPassword(this.getPassword());
        user.setRoleCode(RoleEnum.parents.getValue());
        user.setIsDeleted(false);

        if (Objects.isNull(user.getId())) {
            user.setCreateBy(param.getCreateBy());
            user.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(user.getId())) {
            user.setUpdateBy(param.getUpdateBy());
            user.setUpdateTime(param.getDateInterface().now());
        }
        return user;
    }

    /**
     * 返回家长信息
     *
     * @param param
     * @return
     */
    public Parent getSelf(ImportParam<ParentDTO> param) {
        Parent parent = new Parent();
        parent.setId(this.getId());
        parent.setConfirmed(false);
        parent.setIsActive(false);
        parent.setUserId(this.getUserId());
        parent.setUsername(this.mobile);
        parent.setName(this.name);
        parent.setSchoolId(param.getSchoolId());
        parent.setSex(Sex.getSex(this.getSex()).getCode());
        parent.setMobile(this.mobile);
        parent.setEnabled(true);
        parent.setIsDeleted(false);

        if (Objects.isNull(parent.getId())) {
            parent.setCreateBy(param.getCreateBy());
            parent.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(parent.getId())) {
            parent.setUpdateBy(param.getUpdateBy());
            parent.setUpdateTime(param.getDateInterface().now());
        }
        return parent;
    }

    /**
     * 构造密码
     *
     * @return
     */
    public ParentDTO password() {
        setPassword(this.mobile.substring(this.mobile.length() - 6));
        return this;
    }

    /**
     * 密码
     */
    @EncryptField
    private String password;

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
     * 关系
     */
    @ExcelProperty(index = 2, value = "关系*")
    @ListRange(value = {"父母", "其他监护人"}, message = "关系必须在[父母,其他监护人]之间")
    private String relationship;

    /**
     * 手机号
     */
    @ExcelProperty(index = 3, value = "手机号*")
    @NotNull(message = "手机号不能为空")
    @Pattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式不正确")
    private String mobile;

    /**
     * 学生身份证号
     */
    @ExcelProperty(index = 4, value = "学生身份证号*")
    private String idCardNumbers;

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
         * 关系
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 2, value = "关系（非必填）")
        private String relationship;

        /**
         * 手机号
         */
        @ColumnWidth(value = MIN_WIDTH + 5)
        @ExcelProperty(index = 3, value = "手机号（必填）")
        private String mobile;

        /**
         * 学生身份证号
         */
        @ColumnWidth(value = MIN_WIDTH + 20)
        @ExcelProperty(index = 4, value = "学生身份证号（非必填，多个请用；隔开）")
        private String idCardNumbers;

        /**
         * 错误提示
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 5, value = "错误提示")
        private String errorMsg;
    }
}
