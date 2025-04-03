package com.example.springbootdemo.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.ProcessBo;
import org.apache.ibatis.annotations.Param;

public interface SysProcessMapper extends BaseMapper<ProcessBo> {

    IPage<ProcessBo> processList(@Param("page") IPage page);

}
