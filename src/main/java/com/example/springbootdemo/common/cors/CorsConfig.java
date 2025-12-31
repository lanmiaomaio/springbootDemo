package com.example.springbootdemo.common.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 1. 允许前端域名（生产环境写具体域名，如"http://192.168.101.20:8080"）
        config.addAllowedOrigin("*");
        // 2. 允许携带Cookie和自定义请求头（必须开启，否则Token无法传递）
        config.setAllowCredentials(false);
        // 3. 允许所有请求方法（包括OPTIONS预检请求）
        config.addAllowedMethod("*");
        // 4. 允许自定义请求头（如Authorization）
        config.addAllowedHeader("*");
        // 5. 暴露响应头（让前端能读取后端返回的状态码/数据）
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有接口生效
        return new CorsFilter(source);
    }
}
