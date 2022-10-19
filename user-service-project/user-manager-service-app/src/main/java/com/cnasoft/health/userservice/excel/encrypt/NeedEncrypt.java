package com.cnasoft.health.userservice.excel.encrypt;
import java.lang.annotation.*;

/**
 * @Author JCccc
 * @Description 需加密
 * @Date 2021/7/23 11:55
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedEncrypt {

}