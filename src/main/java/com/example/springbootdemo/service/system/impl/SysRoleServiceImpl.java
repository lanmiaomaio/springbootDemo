package com.example.springbootdemo.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysMenuRole;
import com.example.springbootdemo.model.system.SysRole;
import com.example.springbootdemo.mapper.system.SysRoleMapper;
import com.example.springbootdemo.service.system.ISysMenuRoleService;
import com.example.springbootdemo.service.system.ISysMenuService;
import com.example.springbootdemo.service.system.ISysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private ISysMenuRoleService sysMenuRoleService;

    @Autowired
    private IdentityService  identityService;

    @Override
    public IPage<SysRole> getPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<SysRole> roleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roleLambdaQueryWrapper.orderByDesc(SysRole::getCreateTime);
        IPage<SysRole> page=new Page<>(pageNum,pageSize);
        IPage<SysRole> sysRoleIPage = baseMapper.selectPage(page, roleLambdaQueryWrapper);
        return sysRoleIPage;
    }

    @Override
    @Transactional
    public int add(SysRole sysRole) {
        int add = baseMapper.insert(sysRole);
        if(add==0){
            return add;
        }else{
            Group newGroup = identityService.newGroup(sysRole.getId());
            newGroup.setName(sysRole.getRoleName());
            identityService.saveGroup(newGroup);
            for(String menuId: sysRole.getMenuIds()){
                SysMenuRole sysMenuRole=new SysMenuRole();
                sysMenuRole.setMenuId(menuId);
                sysMenuRole.setRoleId(sysRole.getId());
                sysMenuRoleService.getBaseMapper().insert(sysMenuRole);
            }
            return add;
        }
    }

    @Override
    @Transactional
    public int del(String id) {
        int del = baseMapper.deleteById(id);
        identityService.deleteGroup(id);
        LambdaQueryWrapper<SysMenuRole> menuRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        menuRoleLambdaQueryWrapper.eq(SysMenuRole::getRoleId,id);
        sysMenuRoleService.getBaseMapper().delete(menuRoleLambdaQueryWrapper);
        return del;
    }

    @Override
    public List<Map<String, Object>> getAllMenu() {
        LambdaQueryWrapper<SysMenu> menuLambdaQueryWrapper=new LambdaQueryWrapper<>();
        menuLambdaQueryWrapper.isNull(SysMenu::getParentId).orderByAsc(SysMenu::getSortBy);
        List<SysMenu> sysMenus = sysMenuService.getBaseMapper().selectList(menuLambdaQueryWrapper);
        List<Map<String,Object>> mapList=new ArrayList<>();
        for (SysMenu sysMenu:sysMenus){
            Map<String,Object> map=new HashMap<>();
            map.put("id",sysMenu.getId());
            map.put("label",sysMenu.getName());
            if("0".equals(sysMenu.getStatus())){
                map.put("disabled",true);
            }
            mapList.add(map);
        }
        getChildMenu(mapList);
        return mapList;
    }

    @Override
    @Transactional
    public int edit(SysRole sysRole) {
        LambdaQueryWrapper<SysMenuRole> menuRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        menuRoleLambdaQueryWrapper.eq(SysMenuRole::getRoleId,sysRole.getId());
        sysMenuRoleService.getBaseMapper().delete(menuRoleLambdaQueryWrapper);
        int edit=baseMapper.updateById(sysRole);
        if(edit==0){
            return edit;
        }else{
            identityService.deleteGroup(sysRole.getId());
            Group newGroup = identityService.newGroup(sysRole.getId());
            newGroup.setName(sysRole.getRoleName());
            identityService.saveGroup(newGroup);
            for(String menuId: sysRole.getMenuIds()){
                SysMenuRole sysMenuRole=new SysMenuRole();
                sysMenuRole.setMenuId(menuId);
                sysMenuRole.setRoleId(sysRole.getId());
                sysMenuRoleService.getBaseMapper().insert(sysMenuRole);
            }
            return edit;
        }
    }

    @Override
    public SysRole one(String id) {
        SysRole sysRole = baseMapper.selectById(id);
        LambdaQueryWrapper<SysMenuRole> menuRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        menuRoleLambdaQueryWrapper.eq(SysMenuRole::getRoleId,id);
        List<SysMenuRole> sysMenuRoles = sysMenuRoleService.getBaseMapper().selectList(menuRoleLambdaQueryWrapper);
        String[] stringList = sysMenuRoles.stream().map(menuRole->menuRole.getMenuId()).toArray(String[]::new);
        sysRole.setMenuIds(stringList);
        return sysRole;
    }


    public List<Map<String,Object>> getChildMenu(List<Map<String,Object>> sysMenuIList){
        sysMenuIList.stream().forEach(sysMenu -> {
            LambdaQueryWrapper<SysMenu> menuLambdaQueryWrapper=new LambdaQueryWrapper<>();
            menuLambdaQueryWrapper.eq(SysMenu::getParentId,sysMenu.get("id")).orderByAsc(SysMenu::getSortBy);
            List<SysMenu> sysMenus = sysMenuService.getBaseMapper().selectList(menuLambdaQueryWrapper);
            if(sysMenus.size()!=0){
                List<Map<String,Object>> mapList=new ArrayList<>();
                for (SysMenu sysMenu1:sysMenus){
                    Map<String,Object> map=new HashMap<>();
                    map.put("id",sysMenu1.getId());
                    map.put("label",sysMenu1.getName());
                    if("0".equals(sysMenu1.getStatus())){
                        map.put("disabled",true);
                    }
                    mapList.add(map);
                }
                sysMenu.put("children",mapList);
                getChildMenu(mapList);
            }

        });
        return sysMenuIList;
    }
}
