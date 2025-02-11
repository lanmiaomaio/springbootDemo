package com.example.springbootdemo.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
public interface ISysRoleService extends IService<SysRole> {

    IPage<SysRole> getPage(int pageNum, int pageSize);

    int add(SysRole sysRole);

    int del(String id);

    List<Map<String, Object>> getAllMenu();

    int edit(SysRole sysRole);

    SysRole one(String id);
}





