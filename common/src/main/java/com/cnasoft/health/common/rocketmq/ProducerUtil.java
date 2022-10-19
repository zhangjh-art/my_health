package com.cnasoft.health.common.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 消息生产者工具类
 *
 * @author ganghe
 * @date 2022/9/9 11:11
 **/
@Slf4j
@Component
public class ProducerUtil {
    private static String env;
    private static String nameServerAddress;
    private final static String LOCAL = "local";
    private static final String GROUP = "log-producer-group";
    private static final String TOPIC = "log-topic";

    @Value("${spring.profiles.active}")
    public void setEnv(String env) {
        ProducerUtil.env = env;
    }

    @Value("${spring.cloud.stream.rocketmq.binder.name-server}")
    public void setNameServerAddress(String nameServerAddress) {
        ProducerUtil.nameServerAddress = nameServerAddress;
    }

    /**
     * 发送单向延时10s消息
     *
     * @param msg
     */
    public void sendOnewayDelay(byte[] msg) {
        if (!LOCAL.equals(env)) {
            /*try {
                MQProducer producer = ProducerInstance.getProducerInstance().getInstance(nameServerAddress, GROUP, null);
                //单向消息
                Message message = new Message(TOPIC, msg);
                //此消息将在10秒后发送给消费者
                message.setDelayTimeLevel(3);
                producer.sendOneway(message);
            } catch (MQClientException e) {
                log.error("获取生产者客户端异常", e);
            } catch (RemotingException e) {
                log.error("发送单向消息异常", e);
            } catch (InterruptedException e) {
                log.error("发送消息异常", e);
            }*/
        }
    }
}
