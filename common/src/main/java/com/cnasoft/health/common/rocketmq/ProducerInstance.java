package com.cnasoft.health.common.rocketmq;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ganghe
 * @date 2022/4/6 16:32
 **/
public class ProducerInstance {
    public static final String APPENDER_TYPE = "APPENDER_TYPE";

    public static final String LOG4J_APPENDER = "LOG4J_APPENDER";

    public static final String LOG4J2_APPENDER = "LOG4J2_APPENDER";

    public static final String LOGBACK_APPENDER = "LOGBACK_APPENDER";

    public static final String DEFAULT_GROUP = "rocketmq_appender";

    private ConcurrentHashMap<String, MQProducer> producerMap = new ConcurrentHashMap<>();

    private static ProducerInstance instance = new ProducerInstance();

    public static ProducerInstance getProducerInstance() {
        return instance;
    }

    private String genKey(String nameServerAddress, String group, String instanceName) {
        return nameServerAddress + "_" + group + "_" + instanceName;
    }

    public MQProducer getInstance(String nameServerAddress, String group, String instanceName) throws MQClientException {
        if (StringUtils.isBlank(group)) {
            group = DEFAULT_GROUP;
        }
        if (StringUtils.isBlank(instanceName)) {
            instanceName = "DEFAULT";
        }

        String genKey = genKey(nameServerAddress, group, instanceName);
        MQProducer p = getProducerInstance().producerMap.get(genKey);
        if (p != null) {
            return p;
        }

        DefaultMQProducer defaultMQProducer = new DefaultMQProducer(group);
        defaultMQProducer.setNamesrvAddr(nameServerAddress);
        defaultMQProducer.setInstanceName(instanceName);
        //发送消息超时时间
        defaultMQProducer.setSendMsgTimeout(60000);

        MQProducer beforeProducer;
        beforeProducer = getProducerInstance().producerMap.putIfAbsent(genKey, defaultMQProducer);
        if (beforeProducer != null) {
            return beforeProducer;
        }
        defaultMQProducer.start();
        return defaultMQProducer;
    }

    public void removeAndClose(String nameServerAddress, String group, String instanceName) {
        if (group == null) {
            group = DEFAULT_GROUP;
        }

        if (StringUtils.isBlank(instanceName)) {
            instanceName = "DEFAULT";
        }

        String genKey = genKey(nameServerAddress, group, instanceName);
        MQProducer producer = getProducerInstance().producerMap.remove(genKey);

        if (producer != null) {
            producer.shutdown();
        }
    }

    public void closeAll() {
        Set<Map.Entry<String, MQProducer>> entries = getProducerInstance().producerMap.entrySet();
        for (Map.Entry<String, MQProducer> entry : entries) {
            getProducerInstance().producerMap.remove(entry.getKey());
            entry.getValue().shutdown();
        }
    }
}
