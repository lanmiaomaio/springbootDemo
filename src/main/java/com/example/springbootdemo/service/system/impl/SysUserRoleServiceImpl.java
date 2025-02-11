package com.example.springbootdemo.service.system.impl;

import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysUserRole;
import com.example.springbootdemo.mapper.system.SysUserRoleMapper;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    @Override
    public List<SysMenu> getMenu() {
        return null;
    }
}
