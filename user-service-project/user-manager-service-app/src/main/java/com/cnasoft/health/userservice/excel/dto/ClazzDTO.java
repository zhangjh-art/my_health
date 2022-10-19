package com.cnasoft.health.userservice.excel.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.validator.IValidationErrorResult;
import com.cnasoft.health.userservice.excel.validator.ValidationErrorResult;
import com.cnasoft.health.userservice.model.Clazz;
import com.cnasoft.health.userservice.model.SchoolStaffClazz;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * 班级导入
 *
 * @author ganghe
 * @date 2022/5/16 9:45
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ExcelIgnoreUnannotated
public class ClazzDTO extends ValidationErrorResult implements IValidationErrorResult {

    /**
     * 返回班级信息
     *
     * @param param 参数
     * @return 班级信息
     */
    public Clazz getClazz(ImportParam<ClazzDTO> param) {
        Clazz clazz = new Clazz();
        clazz.setId(this.getId());
        clazz.setGrade(this.grade);
        clazz.setClazzName(this.getClazzName());
        clazz.setSchoolId(param.getSchoolId());
        clazz.setAdmissionDate(UserUtil.getAdmissionYear(this.grade));
        if (Objects.isNull(clazz.getId())) {
            clazz.setCreateBy(param.getCreateBy());
            clazz.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(clazz.getId())) {
            clazz.setUpdateBy(param.getUpdateBy());
            clazz.setUpdateTime(param.getDateInterface().now());
        }
        clazz.setIsDeleted(false);
        return clazz;
    }

    /**
     * 返回班主任信息
     *
     * @param param 参数
     * @return 班主任信息
     */
    public SchoolStaffClazz getStaffClazz(ImportParam<ClazzDTO> param) {
        SchoolStaffClazz staffClazz = new SchoolStaffClazz();
        staffClazz.setId(this.getSchoolStaffClazzId());
        staffClazz.setClazzId(this.getClazzId());
        staffClazz.setSchoolStaffId(this.getSchoolStaffId());
        if (Objects.isNull(staffClazz.getId())) {
            staffClazz.setCreateBy(param.getCreateBy());
            staffClazz.setCreateTime(param.getDateInterface().now());
        }
        if (Objects.nonNull(staffClazz.getId())) {
            staffClazz.setUpdateBy(param.getUpdateBy());
            staffClazz.setUpdateTime(param.getDateInterface().now());
        }
        staffClazz.setIsDeleted(false);
        return staffClazz;
    }

    /**
     * 年级
     */
    @ExcelProperty(index = 0, value = "年级*")
    @NotNull(message = "年级不能为空")
    private String grade;

    /**
     * 班级
     */
    @ExcelProperty(index = 1, value = "班级*")
    @NotNull(message = "班级不能为空")
    private String clazzName;

    /**
     * 班主任手机号码
     */
    @ExcelProperty(index = 2, value = "班主任手机号码*")
    @Pattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "班主任手机号码格式不正确")
    private String mobile;

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
        final static int MIN_WIDTH = 18;

        /**
         * 年级
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 0, value = "年级（必填）")
        private String grade;

        /**
         * 班级
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 1, value = "班级（必填）")
        private String clazzName;

        /**
         * 班主任手机号码
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 2, value = "班主任手机号码（非必填）")
        private String mobile;

        /**
         * 错误提示
         */
        @ColumnWidth(value = MIN_WIDTH)
        @ExcelProperty(index = 3, value = "错误提示")
        private String errorMsg;
    }
}
