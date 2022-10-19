package com.cnasoft.health.userservice.excel.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * author:liukl
 * Date:2022/4/2
 * 某个字段只能填写固定的几个值
 */
@Component
public class IdentifyFieldValueValidator implements ConstraintValidator<IdentifyFieldValue, String> {

    private Class enumClass;
    private String[] values;

    @Override
    public void initialize(IdentifyFieldValue constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.values = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String objVal, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(objVal)) {
            return true;
        }

        String[] targetArr;
        //非某个注解类型
        if (enumClass == DefaultEnum.class) {
            targetArr = values;
        } else {
            //获取某个注解里面所有的属性值
            targetArr = getAllText(enumClass);
        }

        //判断当前字段值是否在自定义的数组或者枚举里
        String obj = Arrays.stream(targetArr).filter(it -> it.equals(objVal)).findAny().orElse(null);
        if (StringUtils.isEmpty(obj)) {
            //返回自定义提示消息
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("字段值只能为:[" + StringUtils.join(targetArr, ",") + "]").addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取某个枚举类型下所有的text值
     * @param enumClass
     * @return
     */
    private static String[] getAllText(Class enumClass) {
        assert enumClass.isEnum();
        String[] arr = null;
        try {
            Enum[] enumConstants = (Enum[]) enumClass.getEnumConstants();
            arr = new String[enumConstants.length];
            //反射获取枚举类中的toString()方法
            Method method = enumClass.getMethod("toString");
            for (int i = 0; i < enumConstants.length; i++) {
                arr[i] = (String) method.invoke(enumConstants[i]);
            }
        } catch (Exception e) {
           // log.error("IdentifyFieldValueValidator getAllText failed! enumClass:{}", enumClass.getName());
            e.printStackTrace();
        }
        return arr;
    }

}