package com.cnasoft.health.userservice.excel.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @Author liukl
 * @Description
 * @Date 2022-04-08
 */
@Slf4j
@Aspect
@Component
public class EncryptAspect {

    @Autowired
    @Qualifier(value = "rsaEncryptor")
    IEncrypt encrypt;

    @Pointcut("@annotation(com.cnasoft.health.userservice.excel.encrypt.NeedEncrypt)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //加密
        encrypt(joinPoint);
        return joinPoint.proceed();
    }

    public void encrypt(ProceedingJoinPoint joinPoint) {
        Object[] objects = null;
        try {
            objects = joinPoint.getArgs();
            if (objects.length != 0) {
                for (int i = 0; i < objects.length; i++) {
                    //抛砖引玉 ，可自行扩展其他类型字段的判断
                    if (objects[i] instanceof String) {
                        objects[i] = encryptValue(objects[i]);
                    } else {
                        encryptObject(objects[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加密对象
     *
     * @param obj
     * @throws IllegalAccessException
     */
    private void encryptObject(Object obj) throws IllegalAccessException {

        if (Objects.isNull(obj)) {
            log.info("当前需要加密的object为null");
            return;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean containEncryptField = field.isAnnotationPresent(EncryptField.class);
            if (containEncryptField) {
                //获取访问权
                field.setAccessible(true);
                String value = encrypt.encrypt(String.valueOf(field.get(obj)));
                field.set(obj, value);
            }
        }
    }

    /**
     * 加密单个值
     *
     * @param realValue
     * @return
     */
    public String encryptValue(Object realValue) {
        try {
            realValue = encrypt.encrypt(String.valueOf(realValue));
        } catch (Exception e) {
            log.info("加密异常={}", e.getMessage());
        }
        return String.valueOf(realValue);
    }
}