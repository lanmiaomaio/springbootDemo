package com.example.springbootdemo.common;

import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class CustomAuthcFilter  extends FormAuthenticationFilter {


    // 未登录/登录过期时调用
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 设置响应头，允许跨域（前后端分离必加）
//        httpResponse.setHeader("Access-Control-Allow-Origin", "ttp://192.168.101.20:8080");
//        httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
//        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
        // 设置响应格式为 JSON
        httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpResponse.setCharacterEncoding("UTF-8");
        // 返回 401 状态码
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 写入自定义提示信息
        PrintWriter writer = httpResponse.getWriter();
        writer.write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\"}");
        writer.flush();
        httpResponse.flushBuffer(); // 强制提交响应，防止后续修改
        writer.close();
        return false; // 不再执行后续过滤器链
    }


    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            return false; // 未携带Token，判定认证无效
        }else{
            boolean expires = JwtUtil.isExpiresAt(token);
            if(expires){
                return false;
            }else{
                return true;
            }
        }
    }

}
