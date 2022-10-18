package com.cnasoft.health.gateway.interceptor;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @function 通过HandlerInterceptorAdapter实现Prometheus自定义监控项监控
 */
@Component
public class PrometheusMetricsInterceptor extends HandlerInterceptorAdapter {
    static {
        // 初始化MeterRegistry
        Metrics.addRegistry(new SimpleMeterRegistry());
    }

    public static final String timerName = "cnasoft.request.timer.statistics";
    private static final Map<String, Timer> functionTimerMap = new ConcurrentHashMap<>();

    Timer.Sample sample;

    /**
     * 业务调用时的前置接口监控
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (!functionTimerMap.containsKey(uri)) {
            String params = uri.substring(1).replaceAll("/", ".").replaceFirst("\\.", ",");
            String[] tags = params.split(",");
            Timer timer = Timer
                    .builder(timerName)
                    .description("http request statics")
                    .tags("service", tags[0], "interface", tags[1], "uri", uri)
                    .register(Metrics.globalRegistry);
            functionTimerMap.put(uri, timer);
        }
        // 记录开始时间
        sample = Timer.start(Metrics.globalRegistry);
        return true;
    }

    /**
     * 业务调用时的后置接口调用
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        sample.stop(functionTimerMap.get(request.getRequestURI()));
    }
}
