package com.example.springbootdemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
;import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize){
        User user=new User();
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
    public ResponseBo edit(@RequestBody User user){
        boolean save = userService.edit(user);
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
    public ResponseBo one(String id){
        if(StringUtils.isNotBlank(id)){
            User one = userService.one(id);
            return ResponseBo.ok(one);
        }else{
            return ResponseBo.error("查询失败");
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/del")
    @Log(title = "删除用户")
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
    public void export(HttpServletResponse response,@RequestBody User user) throws IOException {
        userService.exportList(response,user);
    }

}
