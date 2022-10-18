package com.cnasoft.health.common.constant;

/**
 * 安全相关配置
 */
public interface SecurityConstants {
    /**
     * 用户信息分隔符
     */
    String USER_SPLIT = ":";

    /**
     * 用户登录账号信息头
     */
    String USERNAME_HEADER = "x-username-header";

    /**
     * 用户id信息头
     */
    String USER_ID_HEADER = "x-user-id-header";

    /**
     * 用户姓名信息头
     */
    String USER_NAME_HEADER = "x-user-name-header";

    /**
     * 角色信息头
     */
    String ROLE_HEADER = "x-role-header";

    /**
     * 租户信息头(应用)
     */
    String TENANT_HEADER = "x-tenant-header";

    /**
     * 基础角色
     */
    String BASE_ROLE = "ROLE_USER";

    /**
     * 刷新token
     */
    String REFRESH_TOKEN = "refresh_token";

    /**
     * oauth token
     */
    String OAUTH_TOKEN_URL = "/oauth/token";

    /**
     * 默认的处理验证码的url前缀
     */
    String DEFAULT_VALIDATE_CODE_URL_PREFIX = "/validate/code";

    /**
     * 默认的处理滑块验证码的url前缀
     */
    String CAPTCHA_SLIDER_URL_PREFIX = "/captcha/slider";

    /**
     * 默认生成图形验证码宽度
     */
    String DEFAULT_IMAGE_WIDTH = "100";

    /**
     * 默认生成图像验证码高度
     */
    String DEFAULT_IMAGE_HEIGHT = "35";

    /**
     * 默认生成图形验证码长度
     */
    String DEFAULT_IMAGE_LENGTH = "4";

    /**
     * 默认生成图形验证码过期时间
     */
    int DEFAULT_IMAGE_EXPIRE = 60;

    /**
     * 边框颜色，合法值： r,g,b (and optional alpha) 或者 white,black,blue.
     */
    String DEFAULT_COLOR_FONT = "blue";

    /**
     * 图片边框
     */
    String DEFAULT_IMAGE_BORDER = "no";

    /**
     * 默认图片间隔
     */
    String DEFAULT_CHAR_SPACE = "5";

    /**
     * 默认保存code的前缀
     */
    String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY";

    /**
     * 验证码文字大小
     */
    String DEFAULT_IMAGE_FONT_SIZE = "30";
    /**
     * hm公共前缀
     */
    String HM_PREFIX = "hm:";
    /**
     * 缓存client的redis key，这里是hash结构存储
     */
    String CACHE_CLIENT_KEY = "oauth_client_details";

    /**
     * OAUTH模式登录处理地址
     */
    String OAUTH_LOGIN_PRO_URL = "/user/login";

    /**
     * PASSWORD模式及普通验证码登录处理地址
     */
    String PASSWORD_LOGIN_COMMON_CAPTCHA_URL = "/oauth/pwd_common_captcha/login";

    /**
     * PASSWORD模式滑块验证码登录处理地址
     */
    String PASSWORD_LOGIN_SLIDE_CAPTCHA_URL = "/oauth/pwd_slide_captcha/login";

    /**
     * 新增用户添加到过滤器中
     */
    String NEW_USER_FILTER_CACHE = "/oauth/filter/add_user";

    /**
     * 获取授权码地址
     */
    String AUTH_CODE_URL = "/oauth/authorize";
    /**
     * 登录页面
     */
    String LOGIN_PAGE = "/login.html";

    /**
     * 登录失败页面
     */
    String LOGIN_FAILURE_PAGE = LOGIN_PAGE + "?error";

    /**
     * 默认的OPENID登录请求处理url
     */
    String OPENID_TOKEN_URL = "/oauth/openId/token";

    /**
     * h5手机登陆url
     */
    String H5_MOBILE_TOKEN_URL = "/oauth/h5/mobile/token";
    /**
     * h5用户名密码登陆url
     */
    String H5_PASSWORD_TOKEN_URL = "/oauth/h5/pwd/token";
    /**
     * Pad学生扫码登陆url
     */
    String PAD_USER_ID_TOKEN_URL = "/oauth/pad/token";
    /**
     * Pad测试管理员登陆url
     */
    String PAD_PASSWORD_TOKEN_URL = "/oauth/pad/pwd/token";
    /**
     * 手机登录URL
     */
    String MOBILE_TOKEN_URL = "/oauth/mobile/token";
    /**
     * 登出URL
     */
    String LOGOUT_URL = "/oauth/remove/token";
    /**
     * 默认token过期时间(1小时)
     */
    Integer ACCESS_TOKEN_VALIDITY_SECONDS = 60 * 60;
    /**
     * redis中授权token对应的key
     */
    String REDIS_TOKEN_AUTH = "auth:";
    /**
     * redis中应用对应的token集合的key
     */
    String REDIS_CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    /**
     * redis中用户名对应的token集合的key
     */
    String REDIS_UNAME_TO_ACCESS = "uname_to_access:";
    /**
     * rsa公钥
     */
    String RSA_PUBLIC_KEY = "pubkey.txt";
}
