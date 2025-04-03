package com.example.springbootdemo.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.ProcessBo;
import org.springframework.stereotype.Service;

public interface ISysProcessService {

    IPage<ProcessBo> getPage(int pageNum, int pageSize);
}
