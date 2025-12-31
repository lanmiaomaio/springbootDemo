package com.example.springbootdemo.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Theatre;
import com.example.springbootdemo.model.TheatreEpisode;
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
 * @since 2025-12-14
 */
@RestController
@RequestMapping("/theatre")
public class TheatreController {

    @Autowired
    private ITheatreService theatreService;

    @Autowired
    private ITheatreEpisodeService theatreEpisodeService;

    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize, Theatre theatre) {
        Page<Theatre> theatrePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Theatre> theatreLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreLambdaQueryWrapper.eq(Theatre::getTheatreCategoryId,theatre.getTheatreCategoryId()).orderByDesc(Theatre::getCreateTime);
        IPage<Theatre> theatreIPage = theatreService.page(theatrePage,theatreLambdaQueryWrapper);
        return ResponseBo.ok(theatreIPage);
    }


    /**
     * 添加
     * @param theatre
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加剧")

    public ResponseBo add(@RequestBody Theatre theatre) {
        boolean save = theatreService.save(theatre);

        if (save) {
            return ResponseBo.ok();

        } else {
            return ResponseBo.error();
        }
    }

    /**
     * 修改
     * @param theatre
     * @return
     */

    @PostMapping("/edit")
    @Log(title = "修改剧")

    public ResponseBo edit(@RequestBody Theatre theatre) {
        boolean save = theatreService.updateById(theatre);

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
        Theatre theatre = theatreService.getById(id);
        return ResponseBo.ok(theatre);


    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")

    public ResponseBo one(String id) {
        Theatre theatre = theatreService.getById(id);
        return ResponseBo.ok(theatre);


    }



    @GetMapping("/del")
    @Log(title = "删除剧")
    @Transactional
    public ResponseBo del(String id) {
        boolean save = theatreService.removeById(id);
        if (save) {
            LambdaQueryWrapper<TheatreEpisode> theatreEpisodeLambdaQueryWrapper=new LambdaQueryWrapper<>();
            theatreEpisodeLambdaQueryWrapper.eq(TheatreEpisode::getTheatreId,id);
            List<TheatreEpisode> theatreEpisodeList= theatreEpisodeService.list(theatreEpisodeLambdaQueryWrapper);
            if(theatreEpisodeList.size()>0){
                boolean teRemove = theatreEpisodeService.removeByIds(theatreEpisodeList.stream().map(theatreEpisode -> theatreEpisode.getId()).collect(Collectors.toList()));
                if (teRemove) {
                    return ResponseBo.ok();
                } else {
                    return ResponseBo.error();
                }
            }else{
                return ResponseBo.ok();

            }

        } else {
            return ResponseBo.error();
        }
    }



    //添加集数
    @PostMapping("/addEpisode")
    public ResponseBo addEpisode(@RequestBody TheatreEpisode theatreEpisode) {
        boolean save = theatreEpisodeService.save(theatreEpisode);
        if (save) {
            return ResponseBo.ok();

        } else {
            return ResponseBo.error();
        }
    }

    /**
     * 查询电视剧视频集数
     * @param theatreId
     * @return
     */
    @GetMapping("/selectEpisode")
    public ResponseBo selectEpisode(int pageNum, int pageSize,String theatreId) {
        Page<TheatreEpisode> theatrePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TheatreEpisode> theatreEpisodeLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreEpisodeLambdaQueryWrapper.eq(TheatreEpisode::getTheatreId,theatreId).orderByAsc(TheatreEpisode::getEpisode);
        IPage<TheatreEpisode> theatreEpisodeIPage = theatreEpisodeService.page(theatrePage, theatreEpisodeLambdaQueryWrapper);
        return ResponseBo.ok(theatreEpisodeIPage);


    }


    //添加某个集数信息
    @GetMapping("/viewEpisode")
    public ResponseBo viewEpisode(String episodeId) {
        TheatreEpisode theatreEpisode = theatreEpisodeService.getById(episodeId);

        return ResponseBo.ok(theatreEpisode);


    }

    //修改集数信息
    @PostMapping("/editEpisode")
    public ResponseBo editEpisode(@RequestBody TheatreEpisode theatreEpisode) {
        boolean save = theatreEpisodeService.updateById(theatreEpisode);
        if (save) {
            return ResponseBo.ok();

        } else {
            return ResponseBo.error();
        }
    }

    //删除集数信息
    @GetMapping("/delEpisode")
    public ResponseBo delEpisode(String episodeId) {
        boolean save = theatreEpisodeService.removeById(episodeId);
        if (save) {
            return ResponseBo.ok();

        } else {
            return ResponseBo.error();
        }
    }

}


