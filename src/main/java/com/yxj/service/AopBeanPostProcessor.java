package com.yxj.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author:yuanxj
 * @date:2022/03/25
 */
@Component
public class AopBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if("com.yxj.service.UserService".equals(bean.getClass().getTypeName())){
             Object proxyInstance = Proxy.newProxyInstance(AopBeanPostProcessor.class.getClassLoader(),
                     bean.getClass().getInterfaces(),
                     new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 切面
                    System.out.println("切面操作");

                    return method.invoke(bean,args);
                }
            });

            return proxyInstance;
        }

        return bean;

    }
}
