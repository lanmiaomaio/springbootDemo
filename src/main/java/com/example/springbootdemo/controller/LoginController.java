package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.IpUtil;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.model.system.SysLoginLog;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysLoginLogService;
import com.example.springbootdemo.service.system.ISysMenuService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class LoginController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private ISysLoginLogService sysLoginLogService;

    @PostMapping("/login")
    public ResponseBo login(@RequestBody SysUser user, HttpServletRequest request) {
        String username= user.getUsername();
        SysUser sysUser = sysUserService.findByUserName(username);
        SysLoginLog loginLog=new SysLoginLog();
        loginLog.setLoginUserName(sysUser.getUsername());
        loginLog.setIpAddress(IpUtil.getIpAddr(request));
        if ("1".equals(sysUser.getStatus())) {
            String password=user.getPassword();
            //将用户名密码加密
            String mde5Password = MD5Utils.encrypt(username, password);
            //shiro令牌
            UsernamePasswordToken token = new UsernamePasswordToken(username, mde5Password);
            Subject subject = SecurityUtils.getSubject();
            try {
                subject.login(token);
                String token1 = JwtUtil.getToken(sysUser);
                sysUser.setToken(token1);
                loginLog.setLoginStatus("0");
                sysLoginLogService.save(loginLog);
                return ResponseBo.ok(0,"登录成功",sysUser);
            } catch (UnknownAccountException e) {
                loginLog.setLoginStatus("1");
                sysLoginLogService.save(loginLog);
                return ResponseBo.error(e.getMessage());
            } catch (IncorrectCredentialsException e) {
                loginLog.setLoginStatus("1");
                sysLoginLogService.save(loginLog);
                return ResponseBo.error(e.getMessage());
            } catch (LockedAccountException e) {
                loginLog.setLoginStatus("1");
                sysLoginLogService.save(loginLog);
                return ResponseBo.error(e.getMessage());
            } catch (AuthenticationException e) {
                loginLog.setLoginStatus("1");
                sysLoginLogService.save(loginLog);
                return ResponseBo.error("认证失败！");
            }

        }else{
            return ResponseBo.error("该账号已禁用，请联系管理员！");
        }
    }

    @GetMapping("/leftMenu")
    public ResponseBo leftMenu(){
        List<SysMenu> sysMenus = sysMenuService.leftMenu();
        return ResponseBo.ok(sysMenus);
    }
}
