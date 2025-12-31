package com.example.springbootdemo.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Theatre;
import com.example.springbootdemo.model.TheatreCategory;
import com.example.springbootdemo.model.TheatreEpisode;
import com.example.springbootdemo.service.ITheatreCategoryService;
import com.example.springbootdemo.service.ITheatreEpisodeService;
import com.example.springbootdemo.service.ITheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/front/index")
public class IndexController {

    @Autowired
    private ITheatreService theatreService;

    @Autowired
    private ITheatreEpisodeService theatreEpisodeService;

    @Autowired
    private ITheatreCategoryService theatreCategoryService;


    //分类列表
    @GetMapping("/theatreCategoryList")
    public ResponseBo theatreCategoryList() {
        LambdaQueryWrapper<TheatreCategory> theatreCategoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreCategoryLambdaQueryWrapper.orderByAsc(TheatreCategory::getSort);
        List<TheatreCategory> theatreCategoryList = theatreCategoryService.list(theatreCategoryLambdaQueryWrapper);
        return ResponseBo.ok(theatreCategoryList);
    }

    //剧列表
    @GetMapping("/theatreList")
    public ResponseBo theatreList(Theatre theatre) {
        LambdaQueryWrapper<Theatre> theatreLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreLambdaQueryWrapper.eq(Theatre::getTheatreCategoryId,theatre.getTheatreCategoryId()).orderByDesc(Theatre::getCreateTime);
        List<Theatre> theatreList = theatreService.list(theatreLambdaQueryWrapper);
        return ResponseBo.ok(theatreList);
    }


    /**
     * 剧详情
     * @param id
     * @return
     */
    @GetMapping("/theatreOne")

    public ResponseBo theatreOne(String id) {
        Theatre theatre = theatreService.getById(id);
        return ResponseBo.ok(theatre);


    }

    //通过剧集id查询某个电视剧集数信息
    @GetMapping("/episodeByTheatreId")
    public ResponseBo episodeByTheatreId(String theatreId) {
        LambdaQueryWrapper<TheatreEpisode> theatreEpisodeLambdaQueryWrapper=new LambdaQueryWrapper<>();
        theatreEpisodeLambdaQueryWrapper.eq(TheatreEpisode::getTheatreId,theatreId).orderByAsc(TheatreEpisode::getEpisode);
        List<TheatreEpisode> theatreEpisodeList = theatreEpisodeService.list(theatreEpisodeLambdaQueryWrapper);
        if(theatreEpisodeList.size()>0){
            return ResponseBo.ok(theatreEpisodeList);

        }else{
            return ResponseBo.error(2,"暂无添加视频");
        }


    }


}
