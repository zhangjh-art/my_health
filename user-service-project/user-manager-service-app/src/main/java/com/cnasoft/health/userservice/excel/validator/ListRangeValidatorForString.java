package com.cnasoft.health.userservice.excel.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Objects;

public class ListRangeValidatorForString implements ConstraintValidator<ListRange, String> {

    private String[] range;

    @Override
    public void initialize(ListRange listRange) {
        this.range = listRange.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }
        return Arrays.asList(range).contains(value);
    }
}

