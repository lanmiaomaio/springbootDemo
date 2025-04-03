package com.example.springbootdemo.controller.system;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.ButtonPermission;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDictionaryService sysDictionaryService;


    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ResponseBo page(int pageNum,int pageSize){
        SysUser sysUser=new SysUser();
        IPage<SysUser> page = sysUserService.getPage(pageNum, pageSize,sysUser);
        return ResponseBo.ok(page);
    }

    /**
     * 添加
     * @param sysUser
     * @return
     */
    @PostMapping("/add")
    @ButtonPermission(perm = "sys:user:add")
    @Log(title = "系统管理:添加用户")
    public ResponseBo add(@RequestBody SysUser sysUser){
        sysUser.setPassword("123456");
        sysUser.setPassword(MD5Utils.encrypt(sysUser.getPassword()));
        boolean save = sysUserService.add(sysUser);
        if(save){
            return ResponseBo.ok("添加成功");
        }else{
            return ResponseBo.error("添加失败");
        }
    }


    /**
     * 编辑
     * @param sysUser
     * @return
     */
    @PostMapping("/edit")
    @ButtonPermission(perm = "sys:user:edit")
    @Log(title = "系统管理:修改用户")
    public ResponseBo edit(@RequestBody SysUser sysUser){
        SysDictionary dictionary = sysDictionaryService.getById(sysUser.getPositionId());
        if(dictionary!=null&&("总经理".equals(dictionary.getName())||"部门经理".equals(dictionary.getName()))){
            LambdaQueryWrapper<SysUser> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.ne(SysUser::getId,sysUser.getId());
            userLambdaQueryWrapper.eq(SysUser::getPositionId,dictionary.getId());
            userLambdaQueryWrapper.eq(SysUser::getStatus,"1");
            List<SysUser> userList = sysUserService.list(userLambdaQueryWrapper);
            if(userList.size()!=0){
                return ResponseBo.error("职位中部门经理和总经理每个只能有一个");
            }
        }
        if(StringUtils.isNotBlank(sysUser.getPassword())){
            sysUser.setPassword(MD5Utils.encrypt(sysUser.getPassword()));
        }
        boolean save = sysUserService.edit(sysUser);
        if(save){
            return ResponseBo.ok("修改成功");
        }else{
            return ResponseBo.error("修改失败");
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
//    @ButtonPermission(perm = "sys:user:view")
    public ResponseBo one(String id){
        if(StringUtils.isNotBlank(id)){
            SysUser one = sysUserService.one(id);
            return ResponseBo.ok(one);
        }else{
            return ResponseBo.error("查询失败");
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "系统管理:删除用户")
    public ResponseBo del(String id){
        if(StringUtils.isNotBlank(id)){
            boolean del = sysUserService.del(id);
            if(del){
                return ResponseBo.ok("删除成功");
            }else{
                return ResponseBo.error("删除失败");
            }
        }else{
            return ResponseBo.error("删除失败");
        }
    }


    /**
     * 导出
     * @return
     */
    @GetMapping("/export")
    @ButtonPermission(perm = "sys:user:export")
    public void export(HttpServletResponse response) throws IOException {
        SysUser sysUser=new SysUser();
         sysUserService.exportList(response,sysUser);
    }
}
