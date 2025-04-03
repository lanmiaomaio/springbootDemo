package com.example.springbootdemo.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.mapper.system.SysDictionaryMapper;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-04-25
 */
@Service
public class SysDictionaryServiceImpl extends ServiceImpl<SysDictionaryMapper, SysDictionary> implements ISysDictionaryService {

    @Override
    public IPage<SysDictionary> getPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<SysDictionary> dictionaryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dictionaryLambdaQueryWrapper.isNull(SysDictionary::getParentId);
        dictionaryLambdaQueryWrapper.orderByAsc(SysDictionary::getSortBy).orderByDesc(SysDictionary::getCreateTime);
        IPage<SysDictionary> page=new Page<>(pageNum,pageSize);
        IPage<SysDictionary> dictionaryIPage=baseMapper.selectPage(page,dictionaryLambdaQueryWrapper);
        dictionaryIPage.setRecords(getChildMenu(dictionaryIPage.getRecords()));
        return dictionaryIPage;
    }

    public List<SysDictionary> getChildMenu(List<SysDictionary> dictionaryList){
        dictionaryList.stream().forEach(dictionary -> {
            LambdaQueryWrapper<SysDictionary> dictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dictionaryLambdaQueryWrapper.eq(SysDictionary::getParentId,dictionary.getId()).orderByAsc(SysDictionary::getSortBy);
            List<SysDictionary> dictionaries = baseMapper.selectList(dictionaryLambdaQueryWrapper);
            if(dictionaries.size()!=0){
                dictionary.setChildren(dictionaries);
                getChildMenu(dictionaries);
            }

        });
        return dictionaryList;
    }

    @Override
    public int add(SysDictionary sysDictionary) {
        int insert = baseMapper.insert(sysDictionary);
        return insert;
    }

    @Override
    public int del(String id) {
        int delete = baseMapper.deleteById(id);
        return delete;
    }

    @Override
    public int edit(SysDictionary sysDictionary) {
        int update = baseMapper.updateById(sysDictionary);
        return update;
    }

    @Override
    public SysDictionary one(String id) {
        SysDictionary sysDictionary = baseMapper.selectById(id);
        if (StringUtils.isNotBlank(sysDictionary.getParentId())) {
            SysDictionary sysDictionary1 = baseMapper.selectById(sysDictionary.getParentId());
            sysDictionary.setParentName(sysDictionary1.getName());
        }
        return sysDictionary;
    }

    @Override
    public List<Map<String, Object>> dictionaryList() {
        LambdaQueryWrapper<SysDictionary> dictionaryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dictionaryLambdaQueryWrapper.isNull(SysDictionary::getParentId).orderByAsc(SysDictionary::getSortBy);
        List<SysDictionary> list = baseMapper.selectList(dictionaryLambdaQueryWrapper);
        List<Map<String,Object>> mapList=new ArrayList<>();
        list.stream().forEach(dictionary -> {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("label", dictionary.getName());
                    objectMap.put("value", dictionary.getId());
                    mapList.add(objectMap);
                });
        List<Map<String, Object>> mapList1 = childDictionary(mapList);
        return mapList1;
    }


    public List<Map<String, Object>> childDictionary(List<Map<String,Object>> list){
        list.stream().forEach(map -> {
            LambdaQueryWrapper<SysDictionary> dictionaryLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dictionaryLambdaQueryWrapper.eq(SysDictionary::getParentId,map.get("value")).orderByAsc(SysDictionary::getSortBy);
            List<SysDictionary> sysDictionaryList = baseMapper.selectList(dictionaryLambdaQueryWrapper);
            List<Map<String,Object>> mapList1=new ArrayList<>();
            sysDictionaryList.stream().forEach(dictionary -> {

                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("label", dictionary.getName());
                objectMap.put("value", dictionary.getId());
                objectMap.put("code", dictionary.getCode());
                objectMap.put("parentId", dictionary.getParentId());
                objectMap.put("parentName", map.get("label"));
                mapList1.add(objectMap);
            });
            if(sysDictionaryList.size()!=0){
                map.put("children",mapList1);
            }
            childDictionary(mapList1);

        });
        return list;
    }


}
