package com.example.springbootdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Course;
import com.example.springbootdemo.model.Score;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.service.ICourseService;
import com.example.springbootdemo.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
;import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2024-06-06
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ICourseService courseService;
    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize,User user){
        IPage<User> page = userService.getPage(pageNum, pageSize,user);
        return ResponseBo.ok(page);
    }

    /**
     * 添加
     * @param user
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加用户")
    @ButtonPermission(perm = "user:add")
    public ResponseBo add(@RequestBody User user){

        boolean save = userService.add(user);
        if(save){
            return ResponseBo.ok("添加成功");
        }else{
            return ResponseBo.error("添加失败");
        }
    }


    /**
     * 编辑
     * @param user
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改用户")
    @ButtonPermission(perm = "user:edit")
    public ResponseBo edit(@RequestBody User user){
        boolean save = userService.edit(user);
        if(save){
            return ResponseBo.ok("修改成功");
        }else{
            return ResponseBo.error("修改失败");
        }
    }

    /**
     * 禁用、启用
     * @param id
     * @return
     */
    @GetMapping("/updateStatus")
    @ButtonPermission(perm = "user:disable")
    public ResponseBo updateStatus(String id,String status) {
        if (StringUtils.isNotBlank(id)) {
            boolean update = userService.updateStatus(id,status);
            if(update){
                return ResponseBo.ok();
            }
            return ResponseBo.error();
        } else {
            return ResponseBo.error("修改失败");
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/one")
    @ButtonPermission(perm = "user:view")
    public ResponseBo one(String id) {
        if (StringUtils.isNotBlank(id)) {
            User one = userService.one(id);
            return ResponseBo.ok(one);
        } else {
            return ResponseBo.error("查询失败");
        }
    }

    /**
     * 通过用户id查询成绩
     * @param pageNum
     * @param pageSize
     * @param score
     * @return
     */

    @GetMapping("/scorePage")
    public ResponseBo scorePage(int pageNum, int pageSize,Score score){
        IPage<Score> page  = userService.getScorePage(pageNum, pageSize, score);
        return ResponseBo.ok(page);

    }


    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除用户")
    @ButtonPermission(perm = "user:del")
    public ResponseBo del(String id){
        if(StringUtils.isNotBlank(id)){
            boolean del = userService.del(id);
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
     * 导出
     * @return
             */
    @GetMapping("/export")
    @ButtonPermission(perm = "user:export")
    public void export(User user,HttpServletResponse response) throws IOException {
        userService.exportList(response,user);
    }

    /**
     * 导入
     * @return
     */
    @PostMapping("/importExcel")
    @ButtonPermission(perm = "user:import")
    @Log(title = "导入用户")
    public ResponseBo importList(@RequestParam("file") MultipartFile file) throws Exception {
        try{
            userService.importList(file);
            return ResponseBo.ok();
        }catch (Exception e){
            return ResponseBo.error(e.getMessage());
        }

    }


    /**
     * 通过code查询年级和班级
     * @param gradeCode,classCode
     * @return
     */
    @GetMapping("/getGradeClassByCode")
    public ResponseBo getDictionaryByCode(String gradeCode){
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getType,gradeCode).eq(Course::getStatus,"1").orderByAsc(Course::getSortBy);

        List<Course> list= courseService.list(courseLambdaQueryWrapper);
        List<Map<String, Object>> mapList = courseService.gradeClassList(list);
        return ResponseBo.ok(mapList);
    }
}
