package com.yxj;

import com.spring.ApplicationContext;
import com.yxj.service.UserInterface;
import com.yxj.service.UserService;

/**
 * @Author: YuanXJ
 * @Date: 2022-03-23 22:32
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();
    }
}
