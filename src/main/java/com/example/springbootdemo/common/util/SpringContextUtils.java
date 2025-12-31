package com.example.springbootdemo.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

// 该类被Spring管理，用于获取ApplicationContext
@Component
public class SpringContextUtils implements ApplicationContextAware {

    // 保存Spring上下文对象
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextUtils.applicationContext = context;
    }

    // 静态方法：根据类获取Bean
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    // 静态方法：根据Bean名称获取Bean
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
}
