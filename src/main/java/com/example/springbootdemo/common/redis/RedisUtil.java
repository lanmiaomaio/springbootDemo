package com.example.springbootdemo.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 设置字符串值
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 获取字符串值
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 设置字符串值并设置过期时间（单位：秒）
    public void setWithExpire(String key, String value, long timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    // 判断键是否存在
    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    // 删除键
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    // 其他操作，如 List、Set、Hash 等操作可以类似地封装
}
