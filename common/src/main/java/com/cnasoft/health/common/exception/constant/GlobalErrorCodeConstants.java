package com.cnasoft.health.common.exception.constant;

import com.cnasoft.health.common.exception.ErrorCode;

/**
 * 全局错误码常量
 * [0-999] 系统异常编码保留
 *
 * @author cnasoft
 * @date 2020/8/12 13:49
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(200, "成功");

    // ========== 客户端错误段 ==========

    ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数错误");
    ErrorCode UNAUTHORIZED = new ErrorCode(998, "未授权");
    ErrorCode FORBIDDEN = new ErrorCode(403, "没有该操作权限");
    ErrorCode NOT_FOUND = new ErrorCode(404, "请求未找到");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "不支持的请求方法");
    ErrorCode MEDIA_TYPE_NOT_SUPPORTED = new ErrorCode(415, "不支持的MediaType");
    ErrorCode SERVER_LIMIT = new ErrorCode(429, "服务限流");

    // ========== 服务端错误段 ==========

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "系统异常");

    // ========== 业务异常通用错误段 ==========
    ErrorCode COMMON_BIZ_ERROR = new ErrorCode(600, "业务异常");
    ErrorCode DATE_NOT_EXIST = new ErrorCode(601, "数据不存在");
    ErrorCode SLIDER_CAPTCHA_CHECK_ERROR = new ErrorCode(602, "验证失败");
    ErrorCode DELETE_FAILED = new ErrorCode(603, "删除失败，请检查ID是否存在");
    ErrorCode USERNAME_NOT_EXISTS = new ErrorCode(604, "登录账号不存在");
    ErrorCode USERNAME_DELETED = new ErrorCode(605, "该登录账号已删除");
    ErrorCode USERNAME_NOT_REVIEWED = new ErrorCode(606, "该登录账号审核状态：待审核");
    ErrorCode USERNAME_REJECTED = new ErrorCode(607, "该登录账号审核状态：已拒绝");


    // =========  分布式锁 幂等性错误段 =======
    ErrorCode IDEMPOTENCY_ERROR = new ErrorCode(800, "已存在");
    ErrorCode LOCK_ERROR = new ErrorCode(801, "重复请求");
    ErrorCode SOCKET_TIME_OUT_ERROR = new ErrorCode(802, "请求超时");

    ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");
    ErrorCode AVAILABLE_SERVICE = new ErrorCode(9999, "服务不可用, 请稍后再试!");


    static boolean isMatch(Integer code) {
        return code != null
                && code >= SUCCESS.getCode() && code <= UNKNOWN.getCode();
    }
}
