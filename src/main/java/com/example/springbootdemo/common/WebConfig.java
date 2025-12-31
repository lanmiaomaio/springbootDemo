package com.example.springbootdemo.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${video.output-path}")
    private String hlsPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /hls/** 路径到本地hls目录
        registry.addResourceHandler("/hls/**")
                .addResourceLocations("file:" + new File(hlsPath).getAbsolutePath() + File.separator);
    }
}
