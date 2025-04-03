package com.example.springbootdemo.controller.system;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.ButtonPermission;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysOperLog;
import com.example.springbootdemo.service.system.ISysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-05-04
 */
@RestController
@RequestMapping("/system/operLog")
public class SysOperLogController {

    @Autowired
    private ISysOperLogService sysOperLogService;

    /**
     * 分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize){
        LambdaQueryWrapper<SysOperLog> operLogLambdaQueryWrapper=new LambdaQueryWrapper<>();
        operLogLambdaQueryWrapper.orderByDesc(SysOperLog::getCreateTime);
        IPage<SysOperLog> page=new Page<>(pageNum,pageSize);
         sysOperLogService.page(page, operLogLambdaQueryWrapper);
        return ResponseBo.ok(page);
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
    @ButtonPermission(perm = "sys:operate:view")
    public ResponseBo one(String id){
        SysOperLog sysDictionary= sysOperLogService.getById(id);
        return ResponseBo.ok(sysDictionary);
    }


}
