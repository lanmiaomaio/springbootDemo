package com.example.springbootdemo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.*;
import com.example.springbootdemo.service.ICourseService;
import com.example.springbootdemo.service.IScoreCategoryService;
import com.example.springbootdemo.service.IScoreService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2025-02-18
 */
@RestController
@RequestMapping("/score")
public class ScoreController {


    @Autowired
    private IScoreService scoreService;

    @Autowired
    private IScoreCategoryService scoreCategoryService;

    @Autowired
    private ICourseService courseService;

    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize,Score score){
        IPage<ScoreCategory> page  = scoreService.getPage(pageNum, pageSize, score);
        return ResponseBo.ok(page);

    }

    /**
     * 获取用户列表
     * @paramru
     * @return
     */
    @RequestMapping("/getUserList")
    public ResponseBo getUserList(){
        List<User> list = scoreService.getList();
        return ResponseBo.ok(list);
    }


    /**
     * 添加
     * @param score
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加成绩")
    @ButtonPermission(perm = "score:add")
    public ResponseBo add(@RequestBody Score score){

        int save = scoreService.add(score);
        if(save==1){
            return ResponseBo.ok("添加成功");
        }else{
            return ResponseBo.error(save,"添加失败");
        }
    }


    /**
     * 编辑
     * @param score
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改成绩")
    @ButtonPermission(perm = "score:edit")
    public ResponseBo edit(@RequestBody Score score){
        boolean save = scoreService.edit(score);
        if(save){
            return ResponseBo.ok("修改成功");
        }else{
            return ResponseBo.error("修改失败");
        }
    }

    @PostMapping("/editCategory")
    @Log(title = "修改成绩分类")
    @ButtonPermission(perm = "score:edit")
    public ResponseBo editCategory(@RequestBody ScoreCategory scoreCategory){
        boolean save = scoreCategoryService.updateById(scoreCategory);
        if(save){
            return ResponseBo.ok("修改成功");
        }else{
            return ResponseBo.error("修改失败");
        }
    }

    @GetMapping("/categoryOne")
//    @ButtonPermission(perm = "score:view")
    public ResponseBo categoryOne(String id) {
        if (StringUtils.isNotBlank(id)) {
            ScoreCategory one = scoreCategoryService.getById(id);
            return ResponseBo.ok(one);
        } else {
            return ResponseBo.error("查询失败");
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
//    @ButtonPermission(perm = "score:view")
    public ResponseBo one(String id) {
        if (StringUtils.isNotBlank(id)) {
            Score one = scoreService.one(id);
            return ResponseBo.ok(one);
        } else {
            return ResponseBo.error("查询失败");
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除成绩")
    @ButtonPermission(perm = "score:del")
    public ResponseBo del(String id,String type){
        if(StringUtils.isNotBlank(id)){
            boolean del = scoreService.del(id,type);
            if(del){
                return ResponseBo.ok("删除成功");
            }else{
                return ResponseBo.error("删除失败");
            }
        }else{
            return ResponseBo.error("删除失败");
        }
    }


    /**
     * 导入
     * @return
     */
    @PostMapping("/importExcel")
    @Log(title = "导入成绩")
    @ButtonPermission(perm = "score:import")
    public ResponseBo importList(@RequestParam(value = "file",required = false) MultipartFile file,@RequestParam(value = "score",required = false) String jsonString) throws Exception {
        try{
            Score score = JSON.parseObject(jsonString, Score.class);
            scoreService.importList(file,score);
            return ResponseBo.ok();
        }catch (Exception e){
            return ResponseBo.error(e.getMessage());
        }

    }

    /**
     * 导出
     * @return
     */
    @GetMapping("/export")
    @ButtonPermission(perm = "score:export")
    public void export(Score score, HttpServletResponse response) throws IOException {
//        Score score=new Score();
        scoreService.exportList(response,score);
    }

    /**
     * 成绩统计
     */

    @GetMapping("gradeStatisticsList")
    public ResponseBo gradeStatisticsList(Score score){
        List<ScoreStatistics> gradeStatistics = scoreService.gradeStatistics(score);
        return ResponseBo.ok(gradeStatistics);
    }


    /**
     * 查询班级考试类型数量统计
     */

    @GetMapping("classStatisticsList")
    public ResponseBo classStatisticsList(Score score){
        List<ScoreStatistics> classStatisticsList = scoreService.classStatisticsList(score);
        return ResponseBo.ok(classStatisticsList);
    }

    /**
     * 查询班级成绩统计
     */

    @GetMapping("classDetailStatisticsList")
    public ResponseBo classDetailStatisticsList(Score score){
        List<ScoreStatistics> classDetailStatisticsList = scoreService.classDetailStatisticsList(score);
        return ResponseBo.ok(classDetailStatisticsList);
    }


    @GetMapping("/selectStatisticsDetailPage")
    public ResponseBo selectStatisticsDetailPage(int pageNum, int pageSize,Score score){
        IPage<Score> page  = scoreService.selectStatisticsDetailPage(pageNum, pageSize, score);
        return ResponseBo.ok(page);

    }
    /**
     * 查询课程
     */

    @GetMapping("getCourseList")
    public ResponseBo getCourseList(String id){
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(id)){
            courseLambdaQueryWrapper.eq(Course::getParentId,id);
        }
        courseLambdaQueryWrapper.eq(Course::getType,"scoreCourse").eq(Course::getStatus,"1").orderByAsc(Course::getSortBy);
        List<Course> courseList = courseService.list(courseLambdaQueryWrapper);
        List<Course> distinctCourseList=courseList.stream().collect(
                Collectors.collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(Course::getCode))),
                        ArrayList::new));

        List<Course> sortedPeopleByName = distinctCourseList.stream()
                .sorted(Comparator.comparing(Course::getSortBy))
                .collect(Collectors.toList());
        return ResponseBo.ok(sortedPeopleByName);
    }
}
