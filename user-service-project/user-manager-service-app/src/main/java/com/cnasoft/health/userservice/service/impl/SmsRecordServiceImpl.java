package com.cnasoft.health.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.constant.CommonConstant;
import com.cnasoft.health.common.dto.SmsDTO;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.SmsRecordMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.SmsRecord;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import com.cnasoft.health.userservice.util.SmsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 * @description 针对表【sms_record(短信发送记录)】的数据库操作Service实现
 * @date 2022-05-12 16:58:34
 */
@Service
public class SmsRecordServiceImpl extends SuperServiceImpl<SmsRecordMapper, SmsRecord> implements ISmsRecordService {

    @Value("${user.password.key}")
    private String key;
    @Resource
    private ParentMapper parentMapper;
    @Resource
    private SmsRecordMapper smsRecordMapper;
    @Resource
    private FileFeignClient fileServiceFeign;
    @Resource
    private SysUserMapper userMapper;

    private static final int MINUTE = 60 * 1000;

    @Override
    public Boolean sendSmsVerifyCode(String mobile) {
        //判断是否可以发送短信：
        //1.手机号存在且是家长
        Parent parent = parentMapper.findByMobile(mobile, null, this.key);
        if (parent == null) {
            throw exception("家长不存在");
        }

        Long userId = parent.getUserId();
        sendPublic(mobile, userId, 1);

        return true;
    }

    private void sendPublic(String mobile, Long userId, Integer smsType) {
        SmsRecord lastRecord = this.ifCanSendSms(mobile, userId);
        String verifyCode;
        if (Objects.nonNull(lastRecord)) {
            verifyCode = lastRecord.getContent();
        } else {
            //生成6位随机数字
            verifyCode = SmsUtil.generateVerifyCode();
        }

        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setContent("短信验证码：" + verifyCode + "。" + CommonConstant.SMS_EXPIRE_TIME + "分钟内有效。");
        smsDTO.setMobile(mobile);
        CommonResult<Boolean> smsResult = fileServiceFeign.sendMessage(smsDTO);
        if (Boolean.FALSE.equals(smsResult.getData())) {
            throw exception("短信发送失败,请重试");
        }

        if (Objects.nonNull(lastRecord)) {
            //过期时间20分钟
            lastRecord.setExpireTime(new Date(System.currentTimeMillis() + CommonConstant.SMS_EXPIRE_TIME * MINUTE));
            smsRecordMapper.updateById(lastRecord);
        } else {
            Date now = new Date();
            SmsRecord smsRecord = new SmsRecord();
            smsRecord.setUserId(userId);
            smsRecord.setContent(verifyCode);
            smsRecord.setCreateBy(userId);
            smsRecord.setCreateTime(now);
            smsRecord.setUpdateBy(userId);
            smsRecord.setUpdateTime(now);
            smsRecord.setUsed(0);
            smsRecord.setMobile(mobile);
            //过期时间20分钟
            smsRecord.setExpireTime(new Date(now.getTime() + CommonConstant.SMS_EXPIRE_TIME * MINUTE));
            smsRecord.setSmsType(smsType);
            smsRecordMapper.insert(smsRecord);
        }
    }

    private Boolean sendCaptcha(String mobile, Integer smsType) {
        //判断是否可以发送短信
        //1.手机号存在
        SysUser sysUserQuery = new SysUser();
        sysUserQuery.setMobile(mobile);
        sysUserQuery.setApproveStatus(1);
        sysUserQuery.setEnabled(true);
        SysUser sysUser = userMapper.findOne(sysUserQuery, this.key);
        if (sysUser == null) {
            throw exception("用户不存在");
        }

        Long userId = sysUser.getId();
        sendPublic(mobile, userId, smsType);
        return true;
    }

    @Override
    public Boolean sendSmsForgetPassword(String mobile) {
        return sendCaptcha(mobile, 2);
    }

    @Override
    public Boolean sendSmsLoginCaptcha(String mobile) {
        return sendCaptcha(mobile, 3);
    }

    @Override
    public Boolean sendSmsChangeCaptcha(String mobile) {
        return sendCaptcha(mobile, 4);
    }

    @Override
    public void checkCaptcha(String mobile, String verifyCode) {
        //查询短信发送记录
        if (StringUtils.isEmpty(verifyCode)){
            throw exception("验证码有误");
        }
        SmsRecord smsRecord = smsRecordMapper.selectFirst(mobile, verifyCode);
        if (null == smsRecord || null == smsRecord.getUserId()) {
            throw exception("验证码有误");
        }
        if (smsRecord.getUsed() == 1) {
            throw exception("验证码已使用");
        }
        if (new Date().after(smsRecord.getExpireTime())) {
            throw exception("验证码已失效");
        }
    }

    @Override
    public Boolean sendMobileUpdateCaptcha(String mobile) {
        Long userId = SysUserUtil.getHeaderUserId();

        SysUser sysUser = userMapper.selectOneById(key, userId);
        Assert.isTrue(!sysUser.getMobile().equals(mobile), "新旧手机号不能相同");

        this.ifCanSendSms(mobile, userId);

        // 生成6位随机数字
        String verifyCode = SmsUtil.generateVerifyCode();

        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setContent("您正在修改您的手机号码，短信验证码：" + verifyCode + "。" + CommonConstant.SMS_EXPIRE_TIME + "分钟内有效。");
        smsDTO.setMobile(mobile);
        CommonResult<Boolean> smsResult = fileServiceFeign.sendMessage(smsDTO);
        if (!smsResult.getData()) {
            throw exception("短信发送失败,请重试");
        }
        Date now = new Date();
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setUserId(userId);
        smsRecord.setContent(verifyCode);
        smsRecord.setCreateBy(userId);
        smsRecord.setCreateTime(now);
        smsRecord.setUpdateBy(userId);
        smsRecord.setUpdateTime(now);
        smsRecord.setUsed(0);
        smsRecord.setMobile(mobile);
        // 过期时间20分钟
        smsRecord.setExpireTime(new Date(now.getTime() + CommonConstant.SMS_EXPIRE_TIME * 60 * 1000));
        // 4为修改手机号时发送
        smsRecord.setSmsType(4);
        smsRecordMapper.insert(smsRecord);
        return true;
    }

    /**
     * 是否可以发送短信
     *
     * @param mobile 手机号
     * @param userId 用户id
     */
    private SmsRecord ifCanSendSms(String mobile, Long userId) {

        SysUser sysUser = userMapper.selectOneById(key, userId);
        Assert.isTrue(sysUser != null && sysUser.getApproveStatus().equals(1) && sysUser.getEnabled(), "用户状态不正确");

        SmsRecord lastRecord = smsRecordMapper.selectLast(mobile, userId);
        if (Objects.nonNull(lastRecord) && new Date().before(new Date(lastRecord.getCreateTime().getTime() + CommonConstant.SMS_SEPARATE_TIME * MINUTE))) {
            throw exception("请" + CommonConstant.SMS_SEPARATE_TIME + "分钟后再试");
        }

        //每日短信发送次数是否超标
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_FORMAT);
        String a = sdf.format(today);
        QueryWrapper<SmsRecord> smsRecordQuery = new QueryWrapper<>();
        smsRecordQuery.lambda().eq(SmsRecord::getUserId, userId).eq(SmsRecord::getMobile, mobile).ge(SmsRecord::getCreateTime, a);
        int count = smsRecordMapper.selectCount(smsRecordQuery);

        if (count >= CommonConstant.MAX_SMS_COUNT_EVERYDAY) {
            throw exception("每日最多发送" + CommonConstant.MAX_SMS_COUNT_EVERYDAY + "条");
        }


        if (Objects.nonNull(lastRecord) && lastRecord.getUsed() == 0 && new Date().before(lastRecord.getExpireTime())) {
            return lastRecord;
        } else {
            return null;
        }
    }
}




