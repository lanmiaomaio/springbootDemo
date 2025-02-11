package com.example.springbootdemo.common;

import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 获取用户角色和权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        return null;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String userName = (String) token.getPrincipal();
        String password = new String((char[]) token.getCredentials());
        //数据库匹配用户
        if(StringUtils.isNotBlank(userName)){
            SysUser sysUser = sysUserService.findByUserName(userName);
            if (sysUser == null) {
                throw new UnknownAccountException("用户名或密码错误！");
            }
            if (!password.equals(sysUser.getPassword())) {
                throw new IncorrectCredentialsException("用户名或密码错误！");
            }
            if (("0").equals(sysUser.getStatus())) {
                throw new LockedAccountException("账号已被锁定,请联系管理员！");
            }
            //匹配成功，返回用户信息
            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(sysUser, password, getName());
            return info;
        }else{
            return null;
        }
    }
}
