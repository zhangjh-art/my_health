package com.cnasoft.health.userservice.config;

//import com.cnasoft.health.common.dto.SysOperateLogDTO;
//import com.cnasoft.health.common.util.JsonUtils;
//import com.cnasoft.health.userservice.convert.SysOperateLogConvert;
//import com.cnasoft.health.userservice.service.ISysOperateLogService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.cloud.stream.annotation.StreamListener;
//import org.springframework.cloud.stream.messaging.Sink;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;

/**
 * 日志消息-消费者
 *
 * @author ganghe
 * @date 2022/4/6 21:36
 **/
//@Slf4j
//@Service
//public class LogStreamConsumer {
//    @Resource
//    private ISysOperateLogService operateLogService;
//
//    @StreamListener(Sink.INPUT)
//    public void receive(String messageBody) {
//        if (StringUtils.isNotBlank(messageBody)) {
//            try {
//                SysOperateLogDTO operateLogDTO = JsonUtils.readValue(messageBody, SysOperateLogDTO.class);
//                operateLogService.save(SysOperateLogConvert.INSTANCE.convert(operateLogDTO));
//            } catch (Exception e) {
//                log.error("解析日志内容失败", e);
//            }
//        }
//    }
//}
