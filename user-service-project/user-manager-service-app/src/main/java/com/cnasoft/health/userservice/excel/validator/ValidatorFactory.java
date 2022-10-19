package com.cnasoft.health.userservice.excel.validator;

import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import javax.validation.Validation;
import javax.validation.Validator;

@Configuration
public class ValidatorFactory {
    @Bean
    @ConditionalOnBean(Validator.class)
    public Validator validator(final AutowireCapableBeanFactory autowireCapableBeanFactory) {
        javax.validation.ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .constraintValidatorFactory(new SpringConstraintValidatorFactory(autowireCapableBeanFactory))
                //.addProperty("hibernate.validator.fail_fast", "true") // 只要有一个验证失败，则返回
                .buildValidatorFactory();

        return validatorFactory.getValidator();
    }
}