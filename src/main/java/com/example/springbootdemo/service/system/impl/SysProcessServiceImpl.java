package com.example.springbootdemo.service.system.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.mapper.system.SysProcessMapper;
import com.example.springbootdemo.model.system.ProcessBo;
import com.example.springbootdemo.service.system.ISysProcessService;
import org.springframework.stereotype.Service;

@Service
public class SysProcessServiceImpl extends ServiceImpl<SysProcessMapper, ProcessBo> implements ISysProcessService {
    @Override
    public IPage<ProcessBo> getPage(int pageNum, int pageSize) {
        IPage<ProcessBo> page=new Page<>(pageNum,pageSize);

        IPage<ProcessBo> processBoIPage = baseMapper.processList(page);
        return processBoIPage;
    }
}
