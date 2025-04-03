package com.example.springbootdemo.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.springbootdemo.common.util.IpUtil;
import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysOperLog;
import com.example.springbootdemo.service.system.ISysMenuService;
import com.example.springbootdemo.service.system.ISysOperLogService;
import org.apache.tomcat.util.security.PermissionCheck;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Aspect
@Component
public class PermissionAspect {


    @Autowired
    private ISysMenuService sysMenuService;


    @Pointcut("execution(* *(..)) && @annotation(ButtonPermission)")
    public void permissionCheckPointcut() {}


    @Before("permissionCheckPointcut()")
    public void checkPermission(JoinPoint joinPoint) {
        // 从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 获取切入点所在的方法
        Method method = signature.getMethod();
        ButtonPermission buttonPermission=method.getAnnotation(ButtonPermission.class);


        List<SysMenu> userPermissions = sysMenuService.getButPermission(null);
        List<String> stringList = userPermissions.stream().map(SysMenu::getPermissionIdentity).collect(Collectors.toList());

        if (!stringList.contains(buttonPermission.perm())) {
            throw new RuntimeException("没有权限");
        }
    }

}
