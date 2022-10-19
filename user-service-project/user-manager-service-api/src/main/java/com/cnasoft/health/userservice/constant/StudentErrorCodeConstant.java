package com.cnasoft.health.userservice.constant;

import com.cnasoft.health.common.exception.ErrorCode;

/**
 * 错误码区间 [1-005-000-000 ~ 1-006-000-000)
 *
 * @author zcb
 */
public interface StudentErrorCodeConstant {
    ErrorCode NOT_UPLOAD_FILE = new ErrorCode(1005001100, "请上传文件");
    ErrorCode PARSE_EXCEL_FAILED = new ErrorCode(1005001101, "解析Excel数据失败");
    ErrorCode DUPLICATE  = new ErrorCode(1005001102, "数据存在重复");
    ErrorCode NO_DATA = new ErrorCode(1005001103, "没有数据");
}
