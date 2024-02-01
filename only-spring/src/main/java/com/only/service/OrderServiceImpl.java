package com.only.service;

import com.only.spring.Component;


@Component("orderService")
public class OrderServiceImpl implements OrderService {

    @Override
    public void save() {
        System.out.println("执行 OrderService save");
    }
}
