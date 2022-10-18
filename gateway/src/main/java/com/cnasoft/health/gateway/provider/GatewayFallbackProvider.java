package com.cnasoft.health.gateway.provider;

import cn.hutool.json.JSONObject;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 自定义Zuul回退机制处理器
 * Provides fallback when a failure occurs on a route
 */
@Slf4j
@Component
public class GatewayFallbackProvider implements FallbackProvider {
    /**
     * 返回值表示需要针对此微服务做回退处理(serviceId)
     * *表示为所有微服务提供回退
     *
     * @return
     */

    @Override
    public String getRoute() {
        return "*";
    }

    /**
     * 网关向后端微服务请求是失败了，但是消费者客户端向网关发起的请求是OK的
     * 不应该把api的404,500等问题抛给客户端
     * 网关和后端服务集群对于客户端来说是黑盒
     */

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        log.error("request service {} failed, go fallback.", route);
        if (cause instanceof HystrixTimeoutException) {
            return response(HttpStatus.GATEWAY_TIMEOUT, cause);
        } else {
            return this.fallbackResponse(cause);
        }
    }

    public ClientHttpResponse fallbackResponse(Throwable cause) {
        return this.response(HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    private ClientHttpResponse response(final HttpStatus status, Throwable cause) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return status;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return status.value();
            }

            @Override
            public String getStatusText() throws IOException {
                return status.getReasonPhrase();
            }

            @Override
            public void close() {
            }

            /**
             * 当后端微服务出现宕机后，客户端请求的时候就会返回 fallback降级信息
             *
             * @return
             * @throws IOException
             */

            @Override
            public InputStream getBody() throws IOException {
                JSONObject r = new JSONObject();
                r.set("code", "9999");
                r.set("message", "服务不可用, 请稍后再试!");
                return new ByteArrayInputStream(r.toString().getBytes("UTF-8"));
            }

            @Override
            public HttpHeaders getHeaders() {
                // headers设定
                HttpHeaders headers = new HttpHeaders();
                MediaType mt = new MediaType("application", "json", Charset.forName("UTF-8"));
                headers.setContentType(mt);
                return headers;
            }
        };
    }
}
