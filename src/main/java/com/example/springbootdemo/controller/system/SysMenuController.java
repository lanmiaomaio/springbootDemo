package com.example.springbootdemo.controller.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysRole;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {
    @Autowired
    private ISysMenuService sysMenuService;

    @GetMapping("/page")
    public ResponseBo page(int pageNum,int pageSize){
        IPage<SysMenu> page = sysMenuService.getPage(pageNum, pageSize);
        return ResponseBo.ok(page);
    }

    /**
     * 添加
     * @param sysMenu
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加菜单")
    public ResponseBo add(@RequestBody SysMenu sysMenu){
        int addInt= sysMenuService.add(sysMenu);
        if(addInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }

    /**
     * 编辑
     * @param sysMenu
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改菜单")
    public ResponseBo edit(@RequestBody SysMenu sysMenu){
        int addInt= sysMenuService.edit(sysMenu);
        if(addInt==1){

            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }

    /**
     * 通过id查询
     * @param id
     * @return
     */
    @GetMapping("/one")
    public ResponseBo one(String id){
        SysMenu sysMenu= sysMenuService.one(id);
        return ResponseBo.ok(sysMenu);
    }
    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除菜单")
    public ResponseBo add(String id){
        int delInt= sysMenuService.del(id);
        if(delInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }
}
