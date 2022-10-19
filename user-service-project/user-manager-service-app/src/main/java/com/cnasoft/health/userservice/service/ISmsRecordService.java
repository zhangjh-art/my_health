package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.model.SmsRecord;

/**
 * @author Administrator
 * @description 针对表【sms_record(短信发送记录)】的数据库操作Service
 * @createDate 2022-05-12 16:58:34
 */
public interface ISmsRecordService extends ISuperService<SmsRecord> {
    /**
     * 家长绑定学生时发送短信验证码
     *
     * @param mobile 手机号
     * @return true/false
     */
    Boolean sendSmsVerifyCode(String mobile);

    /**
     * 忘记密码时发送短信
     *
     * @param mobile 手机号
     * @return Boolean
     */
    Boolean sendSmsForgetPassword(String mobile);

    /**
     * 登录时发送短信验证码
     *
     * @param mobile 手机号
     * @return Boolean
     */
    Boolean sendSmsLoginCaptcha(String mobile);

    /**
     * 修改手机号时发送短信验证码
     *
     * @param mobile 手机号
     * @return Boolean
     */
    Boolean sendSmsChangeCaptcha(String mobile);

    /**
     * 校验短信验证码
     * @param mobile 手机号
     * @param verifyCode 验证码
     * @return
     */
    void checkCaptcha(String mobile, String verifyCode);

    /**
     * h5端修改手机号向新手机号发送验证码
     *
     * @param mobile 手机号
     * @return Boolean
     */
    Boolean sendMobileUpdateCaptcha(String mobile);
}
