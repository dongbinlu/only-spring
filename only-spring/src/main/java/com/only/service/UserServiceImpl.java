package com.only.service;

import com.only.spring.Autowired;
import com.only.spring.BeanNameAware;
import com.only.spring.Component;
import com.only.spring.InitializingBean;


@Component("userService")
public class UserServiceImpl implements UserService, InitializingBean, BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String beanName;


    @OnlyValue("only")
    private String name;


    @Override
    public void save() {
        System.out.println("执行 UserService save name," + name + ",beanName : " + beanName + ",orderService : " + orderService);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("执行 InitializingBean afterPropertiesSet 方法");
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
