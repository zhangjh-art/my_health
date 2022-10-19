package com.cnasoft.health.userservice.config;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @date 2022/5/19 17:13
 **/
@Aspect
@Component
public class MybatisAspectj {
    /**
     * 配置织入点
     */
    @Pointcut("execution(public * com.baomidou.mybatisplus.core.mapper.BaseMapper.selectOne(..))")
    public void selectOneAspect() {
        // selectOne执行之前增加limit 1语句
    }

    @Before("selectOneAspect()")
    public void beforeSelect(JoinPoint point) {
        Object arg = point.getArgs()[0];
        if (arg instanceof AbstractWrapper) {
            ((AbstractWrapper<?, ?, ?>) arg).last("limit 1");
        }
    }
}
