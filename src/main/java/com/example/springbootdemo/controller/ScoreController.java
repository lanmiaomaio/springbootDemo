package com.example.springbootdemo.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Score;
import com.example.springbootdemo.model.ScoreStatistics;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.service.IScoreService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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

    @GetMapping("/page")
    public ResponseBo page(int pageNum, int pageSize,Score score){
        IPage<Score> page  = scoreService.getPage(pageNum, pageSize, score);
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
    public ResponseBo edit(@RequestBody Score score){
        boolean save = scoreService.edit(score);
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
    public ResponseBo del(String id){
        if(StringUtils.isNotBlank(id)){
            boolean del = scoreService.del(id);
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
    public void importList(@RequestParam(value = "file",required = false) MultipartFile file) throws Exception {
        Score score=new Score();
        scoreService.importList(file,score);

    }

    /**
     * 导出
     * @return
     */
    @GetMapping("/export")
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
     * 查询班级成绩统计
     */

    @GetMapping("classStatisticsList")
    public ResponseBo classStatisticsList(Score score){
        List<ScoreStatistics> classStatisticsList = scoreService.classStatisticsList(score);
        return ResponseBo.ok(classStatisticsList);
    }
}
