package com.cnasoft.health.userservice.constant;

/**
 * @author cnasoft
 * @date 2020/8/12 16:28
 */
public interface UserConstant {
    String FEIGN_USER = "user-service";
    String FEIGN_BASE_PACKAGE = "com.cnasoft.health.userservice.feign";
    String FEIGN_WARNING_PACKAGE = "com.cnasoft.health.evaluation.feign";
    String LOCK_KEY_USERNAME = "username:";
    String LOCK_KEY_PSYCHIATRIST = "psychiatrist:";

    String LOCK_KEY_RESERVATION = "reservation:";
}
