package com.example.springbootdemo.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.SysLoginLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-04-27
 */
public interface ISysLoginLogService extends IService<SysLoginLog> {

    IPage<SysLoginLog> getPage(int pageNum,int pageSize);

}
