package com.cnasoft.health.userservice.excel.validator;

import com.cnasoft.health.userservice.enums.WhetherEnum;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Data
public class ValidationErrorResult {
    private Long userId;
    private String username;
    private Long studentId;
    private Long studentAdditionalInfoId;
    private List<Long> studentFamilyConditionIds;
    private Long parentId;
    private Long parentUserId;
    private Long id;
    private Long roleId;
    private Long clazzId;
    private Long schoolStaffId;
    private Long schoolStaffClazzId;
    private String errorMsg;
    private Boolean success;

    public Boolean isWhether(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (value.equals(WhetherEnum.TRUE.getDescription())) {
            return true;
        }
        return false;
    }

    public void validate() {
        Validator validator = ImportThreadPool.factory.getValidator();
        Set<ConstraintViolation<ValidationErrorResult>> result = validator.validate(this);
        if (result.size() > 0) {
            this.success = false;
            this.errorMsg = result.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
        } else {
            this.success = true;
        }
    }
}
