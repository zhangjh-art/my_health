package com.cnasoft.health.common.util;

import com.cnasoft.health.common.exception.ServiceException;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.vo.CommonResult;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * @author cnasoft
 * @date 2020/6/22 16:01
 */
public class ResponseUtil {

    private ResponseUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 输出流
     *
     * @param response
     * @param msg        返回信息
     * @param httpStatus 返回状态码
     * @throws IOException
     */
    public static void responseWriter(HttpServletResponse response, String msg, int httpStatus) throws IOException {
        responseWrite(response, CommonResult.error(httpStatus, msg));
    }

    /**
     * 输出成功流
     *
     * @param response
     * @param obj
     */
    public static void responseSucceed(HttpServletResponse response, Object obj) throws IOException {
        responseWrite(response, CommonResult.success(obj));
    }

    /**
     * 输出错误流
     *
     * @param response
     * @param msg
     * @throws IOException
     */
    public static void responseUnAuthorized(HttpServletResponse response, String msg) throws IOException {
        responseWrite(response, CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), msg));
    }

    /**
     * 根据ServiceException输出
     *
     * @param response
     * @param se
     * @throws IOException
     */
    public static void responseFailed(HttpServletResponse response, ServiceException se) throws IOException {
        responseWrite(response, CommonResult.error(se.getCode(), se.getMessage()));
    }

    private static void responseWrite(HttpServletResponse response, CommonResult result) throws IOException {
        response.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8).toString());
        Writer writer = response.getWriter();
        writer.write(JsonUtils.writeValueAsString(result));
        writer.flush();
    }
}
