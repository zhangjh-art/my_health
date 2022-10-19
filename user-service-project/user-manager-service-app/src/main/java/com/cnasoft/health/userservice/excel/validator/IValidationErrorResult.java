package com.cnasoft.health.userservice.excel.validator;

public interface IValidationErrorResult {

    void errorMsg(String message);

    String errorMsg();

    void success(Boolean b);

    Boolean success();

    Boolean isWhether(String value);

}
