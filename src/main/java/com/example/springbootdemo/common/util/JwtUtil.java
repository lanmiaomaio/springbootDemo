package com.example.springbootdemo.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static javax.crypto.Cipher.SECRET_KEY;

public class JwtUtil {

    private static String secretKey="YeBKWjCIxK2viK9lO1J0Vvtr5Yy8C1w4P8bL+lNvcQ8=";

    private static final long EXPIRE_TIME =  60*60*1000;  //过期时间1小时
    //生成token
    public static String getToken(SysUser sysUser) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        String token = "";
        token = JWT.create().withAudience(sysUser.getId()) // 将 userId 保存到 token 里面
                .withClaim("username",sysUser.getUsername())
                .withExpiresAt(date) //1小时后token过期
                .sign(Algorithm.HMAC256(secretKey)); // 以 password 作为 token 的密钥
        return token;
    }

    //获取当前登录的用户id信息
    public static String getCurrentUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("Authorization").replace("Bearer ", "");
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
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            if (!StringUtils.isEmpty(token)) {
                String username = JWT.decode(token).getClaim("username").asString();
                return username;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static boolean isExpiresAt(String token) {
        try {
            // 处理Bearer前缀
            String cleanedToken = token.replace("Bearer ", "");

            // 用com.auth0.jwt解析（与生成用同一个库）
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey))
                    .build()
                    .verify(cleanedToken);

            // 校验过期时间
            return jwt.getExpiresAt().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Token已过期
        } catch (Exception e) {
            e.printStackTrace();
            return true; // 解析失败/签名不匹配
        }
    }

    public static Key getSigningKey(String password) {
        byte[] keyBytes = Base64.getDecoder().decode(password);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
