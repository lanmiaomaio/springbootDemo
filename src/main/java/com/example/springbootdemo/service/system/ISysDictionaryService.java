package com.example.springbootdemo.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.SysDictionary;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.system.SysRole;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-04-25
 */
public interface ISysDictionaryService extends IService<SysDictionary> {

    IPage<SysDictionary> getPage(int pageNum, int pageSize);

    int add(SysDictionary sysRole);

    int del(String id);

    int edit(SysDictionary sysDictionary);

    SysDictionary one(String id);

    List<Map<String, Object>> dictionaryList();

}
