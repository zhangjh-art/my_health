package com.cnasoft.health.userservice.constant;

import com.cnasoft.health.common.exception.ErrorCode;

/**
 * user-service      错误码区间 [1-003-000-000 ~ 1-004-000-000)
 *
 * @author cnasoft
 * @date 2020/8/12 16:24
 */
public interface UserErrorCodeConstants {

    // ========== 用户手机验证码模块 ==========
    ErrorCode USER_SMS_CODE_NOT_FOUND = new ErrorCode(1003001200, "验证码不存在");
    ErrorCode USER_SMS_CODE_EXPIRED = new ErrorCode(1003001201, "验证码已过期");
    ErrorCode USER_SMS_CODE_USED = new ErrorCode(1003001202, "验证码已使用");
    ErrorCode USER_SMS_CODE_NOT_CORRECT = new ErrorCode(1003001203, "验证码不正确");
    ErrorCode USER_SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1003001204, "超过每日短信发送数量");
    ErrorCode USER_SMS_CODE_SEND_TOO_FAST = new ErrorCode(1003001205, "短信发送过于频繁");
    ErrorCode USER_SMS_CODE_NOT_EXPIRED = new ErrorCode(1003001206, "验证码未失效，请失效后再次申请");
    ErrorCode USER_PHONE_NUM_ERROR = new ErrorCode(1003001207, "用户手机号错误");
    ErrorCode SMS_XML_ERROR = new ErrorCode(1003001208, "短信请求返回xml解析失败");
    ErrorCode SMS_PARAM_ERROR = new ErrorCode(1003001209, "短信请求参数编辑失败");


    // ========== 用户信息模块 1004004100 ==========
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1003004100, "用户不存在");
    ErrorCode USER_STATUS_EQUALS = new ErrorCode(1003004101, "用户已经是该状态");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1003004102, "手机号已经存在");
    ErrorCode USER_DISABLED = new ErrorCode(1003004103, "用户被禁用");
    ErrorCode USER_NAME_EXISTS = new ErrorCode(1003004104, "用户名已存在");
    ErrorCode USER_OLD_PASSWORD_ERROR = new ErrorCode(1003004105, "旧密码错误");
    ErrorCode USER_PASSWORD_ERROR = new ErrorCode(1003004106, "用户名或密码错误");
    ErrorCode USER_SELECTION_ERROR = new ErrorCode(1003004107, "角色选择异常");
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1003004108, "角色不存在");
    ErrorCode AUTHORITY_NOT_EXISTS = new ErrorCode(1003004109, "权限不存在");

    ErrorCode USER_ALREADY_BIND = new ErrorCode(1003004110, "用户已被绑定");
    ErrorCode USER_NO_BIND = new ErrorCode(1003004111, "用户未绑定");
    ErrorCode USER_WECHAT_CHECK_TIMEOUT = new ErrorCode(1003004112, "用户微信登录校验超时");
    ErrorCode USER_NO_BEAN_INFO = new ErrorCode(1003004113, "用户没有顺豆数据");
    ErrorCode USER_NO_ID = new ErrorCode(1003004114, "用户id信息为空");
    ErrorCode USER_ALREADY_AGREE = new ErrorCode(1003004115, "用户已同意隐私协议");
    ErrorCode NOT_NEW_AGREEMENT = new ErrorCode(1003004116, "当前协议版本非最新版本");

    ErrorCode AREA_NOT_EXISTS = new ErrorCode(1003004118, "区域数据不存在");

    ErrorCode COMMON_MESSAGE = new ErrorCode(1003004119, "操作失败");
    ErrorCode PARENT_NOT_EXISTS = new ErrorCode(1003004120, "家长不存在");
    ErrorCode SCENE_EXISTS_PASSWORD = new ErrorCode(1003004121, "该场景已设置密码");
    ErrorCode UNKNOWN_SCENE = new ErrorCode(1003004122, "未知场景，请检查");
    ErrorCode PASSWORD_NOT_SAME = new ErrorCode(1003004123, "密码不一致，请重新输入");
    ErrorCode OPERATION_NOT_ALLOWED = new ErrorCode(1003004124, "无权限操作");
    ErrorCode PASSWORD_ERROR = new ErrorCode(1003004125, "密码错误，请重新输入");
}
