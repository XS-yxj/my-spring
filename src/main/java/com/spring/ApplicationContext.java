package com.spring;

import com.yxj.AppConfig;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: YuanXJ
 * @Date: 2022-03-23 22:33
 */
public class ApplicationContext {
    private Class configClass;
    private Map<String,BeanDefinition> beanDefinitionMap = new HashMap<>();
    private Map<String, Object> singletonObjects = new HashMap<>();
    private Map<String,Integer> typeCount = new HashMap<>();
    private Map<String,BeanDefinition> singleBeanDefinitionMap = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    public ApplicationContext(Class configClass) {
        this.configClass = configClass;
        //扫描所有类
        scan(configClass);
        //初始化单实例类并且存放单实力类
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName,bean);
            }
            //存放单个类型的类；
            singleBeanDefinitionMap.put(beanDefinition.getType().getName(),beanDefinition);
        }
    }

    public Object getBean(String beanName){
        if(!beanDefinitionMap.containsKey(beanName)){
            throw new RuntimeException("该类不在容器中");
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition.getScope().equals("singleton")){
            Object singletonBean = singletonObjects.get(beanName);
            if (singletonBean == null) {
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonBean);
            }
            return singletonBean;
        } else {
            // 原型
            Object prototypeBean = createBean(beanName, beanDefinition);
            return prototypeBean;
        }
    }

    private Object getBeanByType(String typeName){
        if (!singleBeanDefinitionMap.containsKey(typeName)) {
            throw new RuntimeException("容器中不存在该类型的类："+typeName);
        }
        BeanDefinition beanDefinition = singleBeanDefinitionMap.get(typeName);
        if(beanDefinition.getScope().equals("singleton")){
            Object singletonBean = singletonObjects.get(typeName);
            if (singletonBean == null) {
                singletonBean = createBean(typeName, beanDefinition);
                singletonObjects.put(typeName, singletonBean);
            }
            return singletonBean;
        } else {
            // 原型
            Object prototypeBean = createBean(typeName, beanDefinition);
            return prototypeBean;
        }
    }

    private void scan(Class configClass){
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            //获取类扫描路径
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace('.', '/');

            //获取类加载器，加载所在路径下的所有类
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());

            if(file.isDirectory()){
                for (File f : file.listFiles()) {
                    String absolutePath = f.getAbsolutePath();

                    absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("\\", ".");

                    try {
                        Class<?> aClass = classLoader.loadClass(absolutePath);
                        if(aClass.isAnnotationPresent(Component.class)){
                            //判断是否实现 BeanPostProcessor
                            if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                                BeanPostProcessor beanPostProcessor = (BeanPostProcessor) aClass.getConstructor().newInstance();
                                beanPostProcessorList.add(beanPostProcessor);
                            }

                            Component componentAnnotation = aClass.getAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            if("".equals(beanName)){
                                beanName = Introspector.decapitalize(aClass.getSimpleName());
                            }


                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setType(aClass);

                            if(aClass.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
                                String value = scopeAnnotation.value();
                                beanDefinition.setScope(value);
                            }else {
                                beanDefinition.setScope("singleton");
                            }

                            beanDefinitionMap.put(beanName,beanDefinition);
                            //计算该类型的类有几个，缓存进map
                            String typeName = aClass.getName();
                            if (typeCount.containsKey(typeName)) {
                                typeCount.put(typeName, typeCount.get(typeName) + 1);
                            } else {
                                typeCount.put(typeName, 1);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition){
        Class aClass = beanDefinition.getType();
        Object instance = null;
        try {
            Constructor constructor = aClass.getConstructor();
            instance = constructor.newInstance();

            for (Field field : aClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    //先判断是否有多个同类型的类
                    Class<?> type = field.getType();
                    String typeName = type.getName();
                    if (!typeCount.containsKey(typeName)) {
                        throw new RuntimeException("容器中不存在该类型的类："+typeName);
                    }
                    if (typeCount.get(typeName)>1){
                        //多个类型，根据名字匹配
                        field.set(instance,getBean(field.getName()));
                    }else {
                        //只有一个类型
                        field.set(instance,getBeanByType(typeName));
                    }
                }
            }

            //初始化前

            //初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //初始化后
            for (BeanPostProcessor processor : beanPostProcessorList) {
                instance = processor.postProcessAfterInitialization(instance, beanName);
            }


        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }
}
