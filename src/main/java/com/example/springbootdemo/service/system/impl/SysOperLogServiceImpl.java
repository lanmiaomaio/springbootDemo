package com.example.springbootdemo.service.system.impl;

import com.example.springbootdemo.model.system.SysOperLog;
import com.example.springbootdemo.mapper.system.SysOperLogMapper;
import com.example.springbootdemo.service.system.ISysOperLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-05-04
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

}
