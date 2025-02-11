package com.example.springbootdemo.controller.system;


import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
@RestController
@RequestMapping("/sys-user-role")
public class SysUserRoleController {

    @Autowired
    private ISysUserRoleService sysUserRoleService;


    @GetMapping
    public ResponseBo getMenu(){
        sysUserRoleService.getMenu();
        return ResponseBo.ok();
    }

}
