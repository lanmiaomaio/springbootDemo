package com.example.springbootdemo.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Course;
import com.example.springbootdemo.service.ICourseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2025-03-19
 */
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize, Course course){
        IPage<Course> page = courseService.getPage(pageNum, pageSize,course);
        return ResponseBo.ok(page);
    }

    /**
     * 添加
     * @param course
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加课程")
    @ButtonPermission(perm = "course:add")
    public ResponseBo add(@RequestBody Course course){

        boolean save = courseService.add(course);
        if(save){
            return ResponseBo.ok("添加成功");
        }else{
            return ResponseBo.error("添加失败");
        }
    }


    /**
     * 编辑
     * @param course
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改课程")
    @ButtonPermission(perm = "course:edit")
    public ResponseBo edit(@RequestBody Course course){
        boolean save = courseService.edit(course);
        if(save){
            return ResponseBo.ok("修改成功");
        }else{
            return ResponseBo.error("修改失败");
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
    @ButtonPermission(perm = "course:view")
    public ResponseBo one(String id) {
        if (StringUtils.isNotBlank(id)) {
            Course one = courseService.one(id);
            return ResponseBo.ok(one);
        } else {
            return ResponseBo.error("查询失败");
        }
    }



    @GetMapping("/getClassCoursePage")
    public ResponseBo getClassCoursePage(int pageNum, int pageSize, Course course){
        IPage<Course> page = courseService.getClassCoursePage(pageNum, pageSize,course);
        return ResponseBo.ok(page);
    }



    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除课程")
    @ButtonPermission(perm = "course:del")
    public ResponseBo del(String id){
        if(StringUtils.isNotBlank(id)){
            boolean del = courseService.del(id);
            if(del){
                return ResponseBo.ok("删除成功");
            }else{
                return ResponseBo.error("删除失败");
            }
        }else{
            return ResponseBo.error("删除失败");
        }
    }

}
