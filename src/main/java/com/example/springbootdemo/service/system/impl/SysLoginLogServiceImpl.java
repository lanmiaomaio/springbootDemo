package com.example.springbootdemo.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.model.system.SysLoginLog;
import com.example.springbootdemo.mapper.system.SysLoginLogMapper;
import com.example.springbootdemo.model.system.SysRole;
import com.example.springbootdemo.service.system.ISysLoginLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-04-27
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

    @Override
    public IPage<SysLoginLog> getPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<SysLoginLog> loginLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        loginLogLambdaQueryWrapper.orderByDesc(SysLoginLog::getCreateTime);
        IPage<SysLoginLog> page=new Page<>(pageNum,pageSize);
        IPage<SysLoginLog> logIPage = baseMapper.selectPage(page, loginLogLambdaQueryWrapper);
        return logIPage;
    }
}
