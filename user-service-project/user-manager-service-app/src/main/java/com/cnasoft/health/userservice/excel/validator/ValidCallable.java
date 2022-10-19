package com.cnasoft.health.userservice.excel.validator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Administrator
 */
@Slf4j
public class ValidCallable<T extends IValidationErrorResult> implements Callable<T> {
    T t;
    Validator validator;

    public ValidCallable(T t, Validator validator) {
        this.t = t;
        this.validator = validator;
        ;
    }

    @SneakyThrows
    @Override
    public T call() {
        StringBuilder sb = new StringBuilder(20);
        Set<ConstraintViolation<T>> result = validator.validate(t);
        if (result.size() > 0) {
            for (val it : result) {
                sb.append(it.getMessage());
                sb.append(";");
            }
            t.success(false);
            String error = sb.toString();
            if (StringUtils.isNotBlank(error)) {
                t.errorMsg(error.substring(0, error.length() - 1));
            }
        } else {
            t.success(true);
        }

        return t;
    }
}
