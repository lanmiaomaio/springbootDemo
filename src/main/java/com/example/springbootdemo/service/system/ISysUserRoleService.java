package com.example.springbootdemo.service.system;

import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
public interface ISysUserRoleService extends IService<SysUserRole> {

    public List<SysMenu> getMenu();

}
