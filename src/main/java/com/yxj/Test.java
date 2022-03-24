package com.yxj;

import com.spring.ApplicationContext;

/**
 * @Author: YuanXJ
 * @Date: 2022-03-23 22:32
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("orderService1"));
        System.out.println(applicationContext.getBean("orderService1"));
        System.out.println(applicationContext.getBean("orderService2"));
    }
}
