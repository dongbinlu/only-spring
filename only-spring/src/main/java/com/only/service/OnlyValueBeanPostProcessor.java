package com.only.service;

import com.only.spring.BeanPostProcessor;
import com.only.spring.Component;

import java.lang.reflect.Field;

@Component
public class OnlyValueBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {


        for (Field field : bean.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(OnlyValue.class)) {
                field.setAccessible(true);
                OnlyValue onlyValue = field.getAnnotation(OnlyValue.class);
                try {
                    field.set(bean, onlyValue.value());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        return bean;
    }
}
