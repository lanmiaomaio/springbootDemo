package com.example.springbootdemo.controller.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysLoginLog;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.service.system.ISysLoginLogService;
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
 * @since 2023-04-27
 */
@RestController
@RequestMapping("/system/loginLog")
public class SysLoginLogController {

    @Autowired
    private ISysLoginLogService sysLoginLogService;

    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize){
        IPage<SysLoginLog> page = sysLoginLogService.getPage(pageNum, pageSize);
        return ResponseBo.ok(page);
    }

}
