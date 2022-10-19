package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.SmsDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.cnasoft.health.common.vo.CommonResult.success;


/**
 * @Author zcb
 * @Time 2022/4/14
 * @Package com.cnasoft.health.userservice.controller
 * @Desc 短信管理
 */
@Slf4j
@RestController
@RequestMapping("/smsRecord")
@Api(tags = "短信记录模块API")
public class SmsRecordController {

    @Resource
    private ISmsRecordService smsRecordService;

    @PostMapping(value = "/sendSmsParentBindStudent")
    @ApiOperation(value = "家长绑定学生发送短信验证码")
    public CommonResult<Boolean> sendSmsVerifyCode(@RequestBody @Validated SmsDTO smsDTO) {
        return success(smsRecordService.sendSmsVerifyCode(smsDTO.getMobile()));
    }

    @PostMapping(value = "/sendSmsForgetPassword")
    @ApiOperation(value = "忘记密码时发送短信验证码")
    public CommonResult<Boolean> sendSmsForgetPassword(@RequestBody @Validated SmsDTO smsDTO) {
        return success(smsRecordService.sendSmsForgetPassword(smsDTO.getMobile()));
    }

    @PostMapping(value = "/sendSmsLoginCaptcha")
    @ApiOperation(value = "登录时发送短信验证码")
    public CommonResult<Boolean> sendSmsLoginCaptcha(@RequestBody @Validated SmsDTO smsDTO) {
        return success(smsRecordService.sendSmsLoginCaptcha(smsDTO.getMobile()));
    }

    @PostMapping(value = "/sendSmsChangeCaptcha")
    @ApiOperation(value = "修改手机时发送短信验证码")
    public CommonResult<Boolean> sendSmsChangeCaptcha(@RequestBody @Validated SmsDTO smsDTO) {
        return success(smsRecordService.sendSmsChangeCaptcha(smsDTO.getMobile()));
    }

    @PostMapping(value = "/sendMobileUpdateCaptcha")
    @ApiOperation(value = "h5修改手机号时向新手机号发送短信验证码")
    public CommonResult<Boolean> sendMobileUpdateCaptcha(@RequestBody @Validated SmsDTO smsDTO) {
        return success(smsRecordService.sendMobileUpdateCaptcha(smsDTO.getMobile()));
    }
}
