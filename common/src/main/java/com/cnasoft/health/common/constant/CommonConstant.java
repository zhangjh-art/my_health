package com.cnasoft.health.common.constant;

/**
 * 全局公共常量
 *
 * @author cnasoft
 * @date 2020/5/18 19:26
 */
public interface CommonConstant {

    String FEIGN_BASE_PACKAGE = "com.cnasoft.health.common.feign";

    /**
     * token请求头名称
     */
    String TOKEN_HEADER = "Authorization";

    /**
     * 传给认证服务器的access_token
     */
    String ACCESS_TOKEN = "access_token";
    String BEARER_TYPE = "Bearer";

    /**
     * 目录
     */
    Integer CATALOG = 1;

    /**
     * 菜单权限
     */
    Integer MENU = 2;

    /**
     * 按钮权限
     */
    Integer PERMISSION = 3;

    /**
     * 超级管理员用户名
     */
    String SUPER_ADMIN_USER_NAME = "ADMIN";

    /**
     * 公共日期格式
     */
    String MONTH_FORMAT = "yyyy-MM";
    String DATE_FORMAT = "yyyy-MM-dd";
    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String SIMPLE_MONTH_FORMAT = "yyyyMM";
    String SIMPLE_DATE_FORMAT = "yyyyMMdd";
    String SIMPLE_DATETIME_FORMAT = "yyyyMMddHHmmss";

    /**
     * 分布式锁前缀
     */
    String LOCK_KEY_PREFIX = "LOCK_KEY:";

    /**
     * 日志链路追踪id信息头
     */
    String TRACE_ID_HEADER = "x-traceid-header";

    /**
     * 日志链路追踪id日志唯一标志
     */
    String LOG_TRACE_ID = "traceId";

    String LOG_B3_TRACEID = "X-B3-TraceId";

    /**
     * 默认密码
     */
    String DEFAULT_USER_PASSWORD = "1234567890";

    /**
     * 官方运营后台
     */
    String ADMIN_PATH = "/admin/";

    /**
     * 老师管理后台
     */
    String BARADMIN_PATH = "/baradmin/";

    /**
     * B端：无需授权验证的接口
     */
    String NOAUTH_BARADMIN_PATH = "/noauth/baradmin/";

    /**
     * 内部服务调用接口
     */
    String INTERNAL_PATH = "/internal/";

    /**
     * C端：无需授权验证的接口
     */
    String NOAUTH_PATH = "/noauth/";

    /**
     * 身份证正则
     */
    String ID_REG =
        "^([1-9][0-9]{5}[0-9]{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)[0-9]{2}[0-9])|([1-9][0-9]{5}(18|19|20)[0-9]{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)[0-9]{3}([0-9]|(X|x)))";

    /**
     * 手机号正则
     */
    String MOBILE_REGEX = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    /**
     * 电子邮件正则
     */
    String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";

    /**
     * 姓名正则
     */
    String NAME_REGEX = "^[\u4e00-\u9fa5]{1,10}";

    //区域心理教研员
    String REGION_PSYCHO_TEACHER = "region_psycho_teacher";

    //校心理老师
    String SCHOOL_PSYCHO_TEACHER = "school_psycho_teacher";

    /**
     * 区域缓存redis key
     */
    String AREA_LIST_KEY = "area_list";

    /**
     * 默认每页条数
     */
    Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页条数
     */
    Integer MAX_PAGE_SIZE = 200;

    String USER_CACHE_PREFIX = "sys_user:";

    /**
     * 用户每日发送短信最大数量
     */
    int MAX_SMS_COUNT_EVERYDAY = 10;

    /**
     * 短信发送间隔时间：1分钟
     */
    int SMS_SEPARATE_TIME = 1;

    /**
     * 验证码有效期：单位分钟
     */
    int SMS_EXPIRE_TIME = 20;

    /**
     * 获取锁的最大尝试时间
     */
    int WAIT_TIME = 10;

    /**
     * 加锁的时间，超过这个时间后锁便自动解锁
     */
    int LEASE_TIME = 60;
}
