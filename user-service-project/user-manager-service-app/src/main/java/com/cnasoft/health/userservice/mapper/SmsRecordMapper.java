package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.userservice.model.SmsRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【sms_record(短信发送记录)】的数据库操作Mapper
* @createDate 2022-05-12 16:58:34
* @Entity com.cnasoft.health.userservice.model.SmsRecord
*/
public interface SmsRecordMapper extends BaseMapper<SmsRecord> {
    /**
     * 查询一条发送记录
     * @param mobile
     * @param content
     * @return
     */
    SmsRecord selectFirst(@Param("mobile") String mobile, @Param("content") String content);

    /**
     * 查询上一条发送记录
     * @param mobile    手机号
     * @param userId    用户id
     * @return SmsRecord    发送记录
     */
    SmsRecord selectLast(@Param("mobile") String mobile, @Param("userId") Long userId);
}




