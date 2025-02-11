package com.example.springbootdemo.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.SysMenu;
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
public interface ISysMenuService extends IService<SysMenu> {

    IPage<SysMenu> getPage(int pageNum,int pageSize);

    List<SysMenu> leftMenu();

    int del(String id);

    int add(SysMenu sysMenu);

    int edit(SysMenu sysMenu);

    SysMenu one(String id);

}
