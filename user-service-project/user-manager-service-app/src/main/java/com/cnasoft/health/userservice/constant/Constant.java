package com.cnasoft.health.userservice.constant;

/**
 * @author ganghe
 */
public interface Constant {
    /**
     * MySQL数据源
     */
    String DATA_SOURCE_MYSQL = "user-service";

    /**
     * MP mapper包路
     */
    String MAPPER_PACKAGE = "com.cnasoft.health.userservice.mapper";

    /**
     * 当前页码
     */
    String PAGE_NUM = "pageNum";

    /**
     * 每页条数
     */
    String PAGE_SIZE = "pageSize";

    /**
     * 每次处理的总数
     */
    Integer BATCH_COUNT = 1000;

    /**
     * 查询条件
     */
    String QUERY = "query";

    /**
     * 查询条件：启用/禁用
     */
    String ENABLED = "enabled";

    /**
     * 查询条件：激活/未激活
     */
    String ACTIVE = "isActive";

    /**
     * 数据字典缓存redis key
     */
    String SYS_DICT_KEY = "sys_dict:";

    /**
     * 数据字典缓存redis key
     */
    String SYS_DICT_DATA_KEY = "sys_dict_data:";

    /**
     * mq group name:创建咨询报告表
     */
    String CONSULTATION_REPORT_GROUP = "CreateConsultationReportGroup";

    /**
     * mq topic name: 添加告警信息
     */
    String ADD_WARNING_TOPIC = "add-warning-topic";

    /**
     * mq group name:添加告警信息
     */
    String ADD_WARNING_GROUP = "AddWarningGroup";
}
