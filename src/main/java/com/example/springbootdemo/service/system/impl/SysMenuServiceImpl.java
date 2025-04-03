package com.example.springbootdemo.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.mapper.system.SysMenuMapper;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public IPage<SysMenu> getPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<SysMenu> menuLambdaQueryWrapper=new LambdaQueryWrapper<>();
        menuLambdaQueryWrapper.isNull(SysMenu::getParentId).orderByAsc(SysMenu::getSortBy);
        IPage<SysMenu> page=new Page<>(pageNum,pageSize);
        IPage<SysMenu> sysMenuIPage = baseMapper.selectPage(page, menuLambdaQueryWrapper);
        sysMenuIPage.setRecords(getChildMenu(sysMenuIPage.getRecords()));
        return sysMenuIPage;
    }

    @Override
    public List<SysMenu> leftMenu() {
        String currentUserId = JwtUtil.getCurrentUserId();
        List<SysMenu> sysMenus;
        if("1".equals(currentUserId)){
            sysMenus = baseMapper.selectLeftMenu(null, null);
        }else{
            sysMenus = baseMapper.selectLeftMenu(currentUserId, null);
        }
        getChildLeftMenu(currentUserId,sysMenus);
        return sysMenus;
    }

    @Override
    public List<SysMenu> getButPermission(String menuId) {
        String currentUserId = JwtUtil.getCurrentUserId();
        List<SysMenu> sysMenus;
        if("1".equals(currentUserId)){
            sysMenus = baseMapper.selectPermission(menuId,null);

        }else{
            sysMenus = baseMapper.selectPermission(menuId,currentUserId);

        }
        return sysMenus;
    }

    public List<SysMenu> getChildLeftMenu(String currentUserId,List<SysMenu> sysMenuIList){
        sysMenuIList.stream().forEach(sysMenu -> {
            List<SysMenu> sysMenus;
            if("1".equals(currentUserId)){
                sysMenus = baseMapper.selectLeftMenu(null, sysMenu.getId());
            }else{
                sysMenus = baseMapper.selectLeftMenu(currentUserId, sysMenu.getId());
            }
            if(sysMenus.size()!=0){
                sysMenu.setChildren(sysMenus);
                getChildLeftMenu(currentUserId,sysMenus);
            }

        });
        return sysMenuIList;
    }

    public List<SysMenu> getChildMenu(List<SysMenu> sysMenuIList){
        sysMenuIList.stream().forEach(sysMenu -> {
            LambdaQueryWrapper<SysMenu> menuLambdaQueryWrapper=new LambdaQueryWrapper<>();
            menuLambdaQueryWrapper.eq(SysMenu::getParentId,sysMenu.getId()).orderByAsc(SysMenu::getSortBy);
            List<SysMenu> sysMenus = baseMapper.selectList(menuLambdaQueryWrapper);
            if(sysMenus.size()!=0){
                sysMenu.setChildren(sysMenus);
                getChildMenu(sysMenus);
            }

        });
        return sysMenuIList;
    }

    @Override
    public int del(String id) {
        int del= baseMapper.deleteById(id);
        return del;
    }

    @Override
    public int add(SysMenu sysMenu) {
        int insert = baseMapper.insert(sysMenu);
        return insert;
    }

    @Override
    public int edit(SysMenu sysMenu) {
        int i = baseMapper.updateById(sysMenu);
        return i;
    }

    @Override
    public SysMenu one(String id) {
        SysMenu sysMenu = baseMapper.selectById(id);
        if(StringUtils.isNotBlank(sysMenu.getParentId())){
           SysMenu sysMenu1= baseMapper.selectById(sysMenu.getParentId());
           sysMenu.setParentName(sysMenu1.getName());
        }
        return sysMenu;
    }

}
