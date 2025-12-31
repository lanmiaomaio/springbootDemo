package com.example.springbootdemo.common.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and() // 启用Spring MVC的跨域配置（关键，否则Security会拦截跨域请求）
                .csrf().disable(); // 开发环境可关闭CSRF，生产环境需配置
    }
}

