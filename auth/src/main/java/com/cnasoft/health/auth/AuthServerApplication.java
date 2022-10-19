package com.cnasoft.health.auth;

import com.cnasoft.health.auth.annotation.EnableFeignInterceptor;
import com.cnasoft.health.auth.constant.OauthConstant;
import com.cnasoft.health.common.rocketmq.ProducerUtil;
import com.cnasoft.health.userservice.constant.UserConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author cnasoft
 * @date 2020/6/16 11:03
 */
@EnableFeignClients(basePackages = {UserConstant.FEIGN_BASE_PACKAGE, OauthConstant.FEIGN_BASE_PACKAGE})
@EnableFeignInterceptor
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication
@Import(ProducerUtil.class)
public class AuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
