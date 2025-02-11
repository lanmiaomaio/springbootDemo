package com.example.springbootdemo.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class JwtUtil {
    @Autowired
    private static ISysUserService sysUserService;

    private static final long EXPIRE_TIME = 60 * 60 * 1000;  //过期时间1小时
    //生成token
    public static String getToken(SysUser sysUser) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        String token = "";
        token = JWT.create().withAudience(sysUser.getId()) // 将 userId 保存到 token 里面
                .withClaim("username",sysUser.getUsername())
                .withExpiresAt(date) //1小时后token过期
                .sign(Algorithm.HMAC256(sysUser.getPassword())); // 以 password 作为 token 的密钥
        return token;
    }

    //获取当前登录的用户id信息
    public static String getCurrentUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (!StringUtils.isEmpty(token)) {
                String userId = JWT.decode(token).getAudience().get(0);
                return userId;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String getCurrentUserName() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (!StringUtils.isEmpty(token)) {
                String username = JWT.decode(token).getClaim("username").asString();
                return username;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
