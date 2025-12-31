package com.example.springbootdemo.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Theatre;
import com.example.springbootdemo.model.TheatreCategory;
import com.example.springbootdemo.model.TheatreEpisode;
import com.example.springbootdemo.service.ITheatreCategoryService;
import com.example.springbootdemo.service.ITheatreEpisodeService;
import com.example.springbootdemo.service.ITheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2025-12-17
 */
@RestController
@RequestMapping("/theatreCategory")
public class TheatreCategoryController {

    @Autowired
    private ITheatreCategoryService theatreCategoryService;

    @Autowired
    private ITheatreService theatreService;

    @Autowired
    private ITheatreEpisodeService theatreEpisodeService;

    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize, TheatreCategory theatreCategory) {
        Page<TheatreCategory> theatrePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TheatreCategory> theatreCategoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreCategoryLambdaQueryWrapper.orderByAsc(TheatreCategory::getSort);
        IPage<TheatreCategory> theatreIPage = theatreCategoryService.page(theatrePage,theatreCategoryLambdaQueryWrapper);
        return ResponseBo.ok(theatreIPage);
    }

    //列表

    @GetMapping("/list")
    public ResponseBo list() {
        LambdaQueryWrapper<TheatreCategory> theatreCategoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreCategoryLambdaQueryWrapper.orderByAsc(TheatreCategory::getSort);
        List<TheatreCategory> theatreCategoryList = theatreCategoryService.list(theatreCategoryLambdaQueryWrapper);
        return ResponseBo.ok(theatreCategoryList);
    }

    /**
     * 添加
     * @param theatreCategory
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加剧类型")

    public ResponseBo add(@RequestBody TheatreCategory theatreCategory) {
        boolean save = theatreCategoryService.save(theatreCategory);

        if (save) {
            return ResponseBo.ok();

        } else {
            return ResponseBo.error();
        }
    }

    /**
     * 修改
     * @param theatreCategory
     * @return
     */

    @PostMapping("/edit")
    @Log(title = "修改剧类型")

    public ResponseBo edit(@RequestBody TheatreCategory theatreCategory) {
        boolean save = theatreCategoryService.updateById(theatreCategory);

        if (save) {
            return ResponseBo.ok();

        } else {
            return ResponseBo.error();
        }
    }


    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/details")

    public ResponseBo details(String id) {
        TheatreCategory theatreCategory = theatreCategoryService.getById(id);
        return ResponseBo.ok(theatreCategory);


    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")

    public ResponseBo one(String id) {
        TheatreCategory theatreCategory = theatreCategoryService.getById(id);
        return ResponseBo.ok(theatreCategory);


    }



    @GetMapping("/del")
    @Log(title = "删除剧类型")
    @Transactional
    public ResponseBo del(String id) {
        boolean save = theatreCategoryService.removeById(id);
        if(save){
            LambdaQueryWrapper<Theatre> theatreLambdaQueryWrapper=new LambdaQueryWrapper<>();
            theatreLambdaQueryWrapper.eq(Theatre::getTheatreCategoryId,id);
            List<Theatre> theatreList = theatreService.list(theatreLambdaQueryWrapper);
            if(theatreList.size()>0){
                boolean remove = theatreService.removeByIds(theatreList.stream().map(theatre -> theatre.getId()).collect(Collectors.toList()));
                if (remove) {
                    theatreList.stream().forEach(theatre -> {
                        LambdaQueryWrapper<TheatreEpisode> theatreEpisodeLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        theatreEpisodeLambdaQueryWrapper.eq(TheatreEpisode::getTheatreId,theatre.getId());
                        List<TheatreEpisode> theatreEpisodes = theatreEpisodeService.list(theatreEpisodeLambdaQueryWrapper);
                        if(theatreEpisodes.size()>0){
                            boolean teremove = theatreEpisodeService.removeByIds(theatreEpisodes.stream().map(theatreEpisode -> theatreEpisode.getId()).collect(Collectors.toList()));
                        }
                    });
                } else {
                    return ResponseBo.error();
                }

            }else{
                return ResponseBo.ok();
            }
            return ResponseBo.ok();
        }else{
            return ResponseBo.error();

        }
    }
}
