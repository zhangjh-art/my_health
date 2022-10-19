package com.cnasoft.health.userservice;

import com.cnasoft.health.common.annotation.approve.EnableApproveRecord;
import com.cnasoft.health.common.util.rocketmq.ProducerUtil;
import com.cnasoft.health.feign.annotation.EnableFeignInterceptor;
import com.cnasoft.health.fileapi.constant.FileConstant;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.UserConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * @author cnasoft
 * @date 2020/8/12 17:38
 */
@EnableFeignClients(basePackages = {UserConstant.FEIGN_BASE_PACKAGE, UserConstant.FEIGN_WARNING_PACKAGE, FileConstant.FEIGN_BASE_PACKAGE})
@EnableFeignInterceptor
@MapperScan(basePackages = {Constant.MAPPER_PACKAGE})
@EnableDiscoveryClient
@SpringBootApplication
@EnableApproveRecord
@Import(ProducerUtil.class)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
