package com.example.springbootdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.common.IpUtil;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.model.Leave;
import com.example.springbootdemo.model.Project;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.model.system.SysLoginLog;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.ILeaveService;
import com.example.springbootdemo.service.IProjectService;
import com.example.springbootdemo.service.IUserService;
import com.example.springbootdemo.service.system.ISysLoginLogService;
import com.example.springbootdemo.service.system.ISysMenuService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class LoginController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private ISysLoginLogService sysLoginLogService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private IProjectService projectService;

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
            String mde5Password = MD5Utils.encrypt(password);
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

    /**
     * 菜单权限
     * @return
     */
    @GetMapping("/leftMenu")
    public ResponseBo leftMenu(){
        List<SysMenu> sysMenus = sysMenuService.leftMenu();
        return ResponseBo.ok(sysMenus);
    }

    /**
     * 按钮权限
     * @return
     */
    @GetMapping("butPermission")
    public ResponseBo butPermission(String menuId){
        List<SysMenu> butPermission = sysMenuService.getButPermission(menuId);
        return ResponseBo.ok(butPermission);
    }


    /**
     * 首页学生人数查询
     * @return
     */
    @GetMapping("userStatistics")
    public ResponseBo userStatistics(){
        String currentUserId = JwtUtil.getCurrentUserId();
        SysUser sysUser = sysUserService.getById(currentUserId);
        List<Map> userStatistics;
        List<LinkedHashMap> mapList=new ArrayList<>();
        Set<String> stringSet=new HashSet<>();
        if("1".equals(sysUser.getId())){
            userStatistics = userService.userStatistics(null);
            userStatistics.stream().forEach(user->{
                LinkedHashMap map=new LinkedHashMap();
                map.put("gradeName",user.get("gradeName"));
                String[] className=user.get("className").toString().split(",");
                String[] classCount=user.get("classCount").toString().split(",");
                for (int i=0;i<className.length;i++){
                    map.put(className[i], classCount[i]);
                    stringSet.add(className[i]);
                }
                mapList.add(map);
            });
        }else if(StringUtils.isNotBlank(sysUser.getClasss())){
            userStatistics= userService.userStatistics(sysUser.getClasss().split(","));
            userStatistics.stream().forEach(user->{
                LinkedHashMap map=new LinkedHashMap();
                map.put("gradeName",user.get("gradeName"));
                String[] className=user.get("className").toString().split(",");
                String[] classCount=user.get("classCount").toString().split(",");
                for (int i=0;i<className.length;i++){
                    map.put(className[i], classCount[i]);
                    stringSet.add(className[i]);
                }
                mapList.add(map);
            });
        }
        Map map=new HashMap();
        map.put("data",mapList);
        map.put("string",stringSet);
        return ResponseBo.ok(map);
    }


    @GetMapping("overallStatistics")
    public ResponseBo overallStatistics(){
        String currentUserId = JwtUtil.getCurrentUserId();
        SysUser sysUser = sysUserService.getById(currentUserId);
        LambdaQueryWrapper<User> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getStatus,"be4521e9b35b81ffc52eee3b9eff01c4");
        int userCount=0;
        if(!"1".equals(currentUserId)){
            if(StringUtils.isNotBlank(sysUser.getClasss())){
                userLambdaQueryWrapper.in(User::getClasss,sysUser.getClasss().split(","));
                userCount = userService.count(userLambdaQueryWrapper);

            }
        }else{
            userCount = userService.count(userLambdaQueryWrapper);
        }
        Map map=new HashMap();
        map.put("userCount",userCount);

        LambdaQueryWrapper<User> userSexLambdaQueryWrapper1=new LambdaQueryWrapper<>();
        userSexLambdaQueryWrapper1.eq(User::getStatus,"be4521e9b35b81ffc52eee3b9eff01c4").eq(User::getGender,"1");

        int nanSexCount=0;
        if(!"1".equals(currentUserId)){
            if(StringUtils.isNotBlank(sysUser.getClasss())){
                userSexLambdaQueryWrapper1.in(User::getClasss,sysUser.getClasss().split(","));
                nanSexCount = userService.count(userSexLambdaQueryWrapper1);
            }
        }else {
            nanSexCount = userService.count(userSexLambdaQueryWrapper1);
        }
        map.put("nanSexCount",nanSexCount);
        map.put("nvSexCount",userCount-nanSexCount);

        LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper=new LambdaQueryWrapper<>();
        leaveLambdaQueryWrapper.eq(Leave::getProcessStatus,"2");

        int leaveCount=0;
        if(!"1".equals(currentUserId)){
            leaveLambdaQueryWrapper.eq(Leave::getUserId,currentUserId);
            leaveCount = leaveService.count(leaveLambdaQueryWrapper);
        }else {
            leaveCount = leaveService.count(leaveLambdaQueryWrapper);
        }

        map.put("leaveCount",leaveCount);

        LambdaQueryWrapper<Project> projectLambdaQueryWrapper=new LambdaQueryWrapper<>();
        projectLambdaQueryWrapper.eq(Project::getProcessStatus,"2");

        int projectCount=0;
        if(!"1".equals(currentUserId)){
            projectLambdaQueryWrapper.eq(Project::getUserId,currentUserId);
            projectCount = projectService.count(projectLambdaQueryWrapper);
        }else {
            projectCount = projectService.count(projectLambdaQueryWrapper);
        }
        map.put("projectCount",projectCount);


        return ResponseBo.ok(map);
    }
}
