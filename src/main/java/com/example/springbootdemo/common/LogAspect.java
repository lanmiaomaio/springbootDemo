package com.example.springbootdemo.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.springbootdemo.model.system.SysLoginLog;
import com.example.springbootdemo.model.system.SysOperLog;
import com.example.springbootdemo.service.system.ISysLoginLogService;
import com.example.springbootdemo.service.system.ISysOperLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
public class LogAspect {

    /**
     * 统计请求的处理时间
     */
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Autowired
    private ISysOperLogService sysOperLogService;

    /**
     * @methodName：logPoinCut
     * @description：设置操作日志切入点 记录操作日志 在注解的位置切入代码
     * @author：tanyp
     * @dateTime：2021/11/18 14:22
     * @Params： []
     * @Return： void
     * @editNote：
     */
    @Pointcut("@annotation(com.example.springbootdemo.common.Log)")
    public void logPoinCut() {
    }


    @Before("logPoinCut()")
    public void doBefore() {
        // 接收到请求，记录请求开始时间
        startTime.set(System.currentTimeMillis());
    }

    /**
     * @methodName：doAfterReturning
     * @description：正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     * @author：tanyp
     * @dateTime：2021/11/18 14:21
     * @Params： [joinPoint, keys]
     * @Return： void
     * @editNote：
     */
    @AfterReturning(value = "logPoinCut()", returning = "keys")
    public void doAfterReturning(JoinPoint joinPoint, Object keys) {
        Object[] o = joinPoint.getArgs();
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        SysOperLog operLog = new SysOperLog();
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            // 获取切入点所在的方法
            Method method = signature.getMethod();

            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();

            // 获取操作
            Log log = method.getAnnotation(Log.class);
            if (Objects.nonNull(log)) {
                operLog.setTitle(log.title());
            }
            String params="";
            Map<String, String> rtnMap = converMap(request.getParameterMap());
            if(("application/json").equals(request.getContentType())){
                params = JSONObject.toJSONString(o[0]);
            }else {
                params = JSON.toJSONString(rtnMap);
            }
            operLog.setMethod(className + "." + method.getName()); // 请求的方法名
            operLog.setReqParam(params); // 请求参数
            operLog.setResParam(JSON.toJSONString(keys)); // 返回结果
            operLog.setUserId(JwtUtil.getCurrentUserId()); // 请求用户ID
            operLog.setUsername(JwtUtil.getCurrentUserName()); // 请求用户名称
            operLog.setIpAddress(IpUtil.getIpAddr(request)); // 请求IP
            operLog.setUri(request.getRequestURI()); // 请求URI
//            operLog.setCreateTime(LocalDateTime.now()); // 创建时间
            sysOperLogService.save(operLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * @methodName：doAfterThrowing
//     * @description：异常返回通知，用于拦截异常日志信息 连接点抛出异常后执行
//     * @author：tanyp
//     * @dateTime：2021/11/18 14:23
//     * @Params： [joinPoint, e]
//     * @Return： void
//     * @editNote：
//     */
//    @AfterThrowing(pointcut = "exceptionLogPoinCut()", throwing = "e")
//    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
//        // 获取RequestAttributes
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//
//        // 从获取RequestAttributes中获取HttpServletRequest的信息
//        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
//
//        try {
//            // 从切面织入点处通过反射机制获取织入点处的方法
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//
//            // 获取切入点所在的方法
//            Method method = signature.getMethod();
//
//            // 获取请求的类名
//            String className = joinPoint.getTarget().getClass().getName();
//
//            logErrorInfoService.save(
//                    LogErrorInfo.builder()
//                            .id(UUID.randomUUID().toString())
//                            .reqParam(JSON.toJSONString(converMap(request.getParameterMap()))) // 请求参数
//                            .method(className + "." + method.getName()) // 请求方法名
//                            .name(e.getClass().getName()) // 异常名称
//                            .message(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace())) // 异常信息
//                            .userId(SecurityUserUtils.getUser().getId()) // 操作员ID
//                            .userName(SecurityUserUtils.getUser().getUsername()) // 操作员名称
//                            .uri(request.getRequestURI()) // 操作URI
//                            .ip(IPUtils.getIpAddress(request)) // 操作员IP
//                            .version(version) // 版本号
//                            .createTime(LocalDateTime.now()) // 发生异常时间
//                            .build()
//            );
//        } catch (Exception e2) {
//            e2.printStackTrace();
//        }
//    }

    /**
     * @methodName：converMap
     * @description：转换request 请求参数
     * @author：tanyp
     * @dateTime：2021/11/18 14:12
     * @Params： [paramMap]
     * @Return： java.util.Map<java.lang.String, java.lang.String>
     * @editNote：
     */
    public Map<String, String> converMap(Map<String, String[]> paramMap) {
        Map<String, String> rtnMap = new HashMap<String, String>();
        for (String key : paramMap.keySet()) {
            rtnMap.put(key, paramMap.get(key)[0]);
        }
        return rtnMap;
    }
}
