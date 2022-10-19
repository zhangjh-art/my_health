package com.cnasoft.health.userservice.excel.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 *  * author:liukl
 *  * Date:2022/4/2
 * 当前值必须在给定的列表中
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {ListRangeValidatorForString.class})
public @interface ListRange {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] value() default {};
}
