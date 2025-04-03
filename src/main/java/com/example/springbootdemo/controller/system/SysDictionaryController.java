package com.example.springbootdemo.controller.system;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-04-25
 */
@RestController
@RequestMapping("/system/dictionary")
public class SysDictionaryController {

    @Autowired
    private ISysDictionaryService sysDictionaryService;

    /**
     * 分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize){
        IPage<SysDictionary> page = sysDictionaryService.getPage(pageNum, pageSize);
        return ResponseBo.ok(page);
    }


    /**
     * 添加
     * @param sysDictionary
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加数据字典")
    @ButtonPermission(perm = "sys:dictionary:add")
    public ResponseBo add(@RequestBody SysDictionary sysDictionary){
        int addInt= sysDictionaryService.add(sysDictionary);
        if(addInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
    @ButtonPermission(perm = "sys:dictionary:view")
    public ResponseBo one(String id){
        SysDictionary sysDictionary= sysDictionaryService.one(id);
        return ResponseBo.ok(sysDictionary);
    }

    /**
     * 编辑
     * @param sysDictionary
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改数据字典")
    @ButtonPermission(perm = "sys:dictionary:edit")
    public ResponseBo edit(@RequestBody SysDictionary sysDictionary){
        int addInt= sysDictionaryService.edit(sysDictionary);
        if(addInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }


    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除数据字典")
    @ButtonPermission(perm = "sys:dictionary:del")
    public ResponseBo del(String id){
        int delInt= sysDictionaryService.del(id);
        if(delInt==1){
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();
        }
    }


    /**
     * 获取所有父id
     * @return
     */
    @GetMapping("/dictionaryList")
    public ResponseBo list(){
        List<Map<String, Object>> list= sysDictionaryService.dictionaryList();
        return ResponseBo.ok(list);
    }

    /**
     * 通过code查询
     * @param type
     * @return
     */
    @GetMapping("/getDictionaryByCode")
    public ResponseBo getDictionaryByCode(String type){
        LambdaQueryWrapper<SysDictionary> dictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dictionaryLambdaQueryWrapper.eq(SysDictionary::getCode,type).orderByAsc(SysDictionary::getSortBy);
        List<SysDictionary> list= sysDictionaryService.list(dictionaryLambdaQueryWrapper);
        return ResponseBo.ok(list);
    }

    /**
     * 通过code查询
     * @param id
     * @return
     */
    @GetMapping("/getDictionaryByParentId")
    public ResponseBo getDictionaryByParentId(String id){
        LambdaQueryWrapper<SysDictionary> dictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dictionaryLambdaQueryWrapper.eq(SysDictionary::getParentId,id).orderByAsc(SysDictionary::getSortBy);
        List<SysDictionary> list= sysDictionaryService.list(dictionaryLambdaQueryWrapper);
        return ResponseBo.ok(list);
    }

}
