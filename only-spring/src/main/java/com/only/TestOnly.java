package com.only;

import com.only.config.AppConfig;
import com.only.service.UserService;
import com.only.spring.AnnotationConfigApplicationContext;

public class TestOnly {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        UserService userService = (UserService)context.getBean("userService");
        System.out.println(userService);
        userService.save();

    }


}
