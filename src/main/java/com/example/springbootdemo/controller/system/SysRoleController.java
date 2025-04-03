package com.example.springbootdemo.controller.system;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.ButtonPermission;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysRole;
import com.example.springbootdemo.service.system.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;


    /**
     * 分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ResponseBo page(int pageNum,int pageSize){
        IPage<SysRole> page = sysRoleService.getPage(pageNum, pageSize);
        return ResponseBo.ok(page);
    }


    /**
     * 添加
     * @param sysRole
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加角色")
    @ButtonPermission(perm = "sys:role:add")
    public ResponseBo add(@RequestBody SysRole sysRole){
        int addInt= sysRoleService.add(sysRole);
        if(addInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
    @ButtonPermission(perm = "sys:role:view")
    public ResponseBo one(String id){
        SysRole sysRole= sysRoleService.one(id);
        return ResponseBo.ok(sysRole);
    }

    /**
     * 编辑
     * @param sysRole
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改角色")
    @ButtonPermission(perm = "sys:role:edit")
    public ResponseBo edit(@RequestBody SysRole sysRole){
        int addInt= sysRoleService.edit(sysRole);
        if(addInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }


    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除角色")
    @ButtonPermission(perm = "sys:role:del")
    public ResponseBo del(String id){
        int delInt= sysRoleService.del(id);
        if(delInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }

    /**
     * 获取角色列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public ResponseBo list(){
        LambdaQueryWrapper<SysRole> roleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        roleLambdaQueryWrapper.orderByDesc(SysRole::getCreateTime);
        List<SysRole> list = sysRoleService.list(roleLambdaQueryWrapper);
        return ResponseBo.ok(list);

    }


    @GetMapping("/getAllMenu")
    public ResponseBo getAllMenu(){
        List<Map<String,Object>> map= sysRoleService.getAllMenu();
        return ResponseBo.ok(map);
    }
}
