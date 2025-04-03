package com.example.springbootdemo.common.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块
     * @return
     */
    String title() default "";

}
