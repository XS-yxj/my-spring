package com.yxj.service;

import com.spring.Autowired;
import com.spring.Component;
import com.spring.InitializingBean;

/**
 * @Author: YuanXJ
 * @Date: 2022-03-23 22:32
 */
@Component("userService")
public class UserService implements UserInterface {

    @Autowired
    private OrderService orderService1;

    @Autowired
    private OrderService orderService2;

    @Override
    public void test(){
        System.out.println(orderService1);
        System.out.println(orderService2);
    }

}
