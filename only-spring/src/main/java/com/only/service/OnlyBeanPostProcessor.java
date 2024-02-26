package com.only.service;

import com.only.spring.BeanPostProcessor;
import com.only.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class OnlyBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(OnlyBeanPostProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                            // 执行切面逻辑
                            System.out.println("执行切面逻辑,method:"+ method.getName());

                            // 注意这里，真正执行目标方法的是普通bean，不是代理对象
                            return method.invoke(bean, args);
                        }
                    });
            return proxyInstance;
        }
        return bean;
    }
}
