package com.cnasoft.health.auth.constant;

import com.cnasoft.health.common.exception.ErrorCode;

/**
 * auth-service      错误码区间 [1-002-000-000 ~ 1-003-000-000)
 *
 * @author cnasoft
 * @date 2020/8/14 15:11
 */
public interface AuthErrorCodeConstants {

    ErrorCode DEVICEID_MISSING = new ErrorCode(1002001100, "请在请求参数中携带deviceId参数");
    ErrorCode VALIDATA_CODE_MISSING = new ErrorCode(1002001101, "请填写验证码");
    ErrorCode VALIDATA_CODE_EXPIRED = new ErrorCode(1002001102, "验证码不存在或已过期");
    ErrorCode VALIDATA_CODE_ERROR = new ErrorCode(1002001103, "验证码不正确");

}
