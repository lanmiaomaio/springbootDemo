package com.example.springbootdemo.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.model.Score;
import com.example.springbootdemo.mapper.ScoreMapper;
import com.example.springbootdemo.model.ScoreStatistics;
import com.example.springbootdemo.model.excelVo.ScoreExcel;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.model.excelVo.ScoreExpotExcel;
import com.example.springbootdemo.service.IScoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.IUserService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2025-02-18
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements IScoreService {
    @Autowired
    private IUserService userService;

    @Override
    public IPage<Score> getPage(int pageNum, int pageSize, Score score) {
        Page<Score> page = new Page<>(pageNum, pageSize);
        IPage<Score> scoreIPage = baseMapper.selectPage(page, score);
        return scoreIPage;
    }

    @Override
    public List<User> getList() {
        LambdaQueryWrapper<User> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        List<User> list = userService.list(userLambdaQueryWrapper);
        return list;
    }

    @Override
    public int add(Score score) {
        User user = userService.getById(score.getUserId());
        LambdaQueryWrapper<Score> scoreLambdaQueryWrapper=new LambdaQueryWrapper<>();
        scoreLambdaQueryWrapper.eq(Score::getUserId,score.getUserId());
        scoreLambdaQueryWrapper.eq(Score::getSemester,score.getSemester());
        scoreLambdaQueryWrapper.eq(Score::getGrade,user.getGrade());
        List<Score> scores = baseMapper.selectList(scoreLambdaQueryWrapper);
        if(scores.size()==1){
            return 2;
        }
        score.setClasss(user.getClasss());
        score.setGrade(user.getGrade());
        int totalScore=score.getChinese()==null?0:score.getChinese()+(score.getEnglish()==null?0:score.getEnglish())+(score.getMathematics()==null?0:score.getMathematics())+(score.getGeneralScience()==null?0:score.getGeneralScience());
        score.setTotalScore(totalScore);
        int insert = baseMapper.insert(score);
        return insert;
    }

    @Override
    public boolean edit(Score score) {
        int totalScore=score.getChinese()==null?0:score.getChinese()+(score.getEnglish()==null?0:score.getEnglish())+(score.getMathematics()==null?0:score.getMathematics())+(score.getGeneralScience()==null?0:score.getGeneralScience());
        score.setTotalScore(totalScore);
        int update = baseMapper.updateById(score);
        if(update==1){
            return true;
        }
        return false;
    }

    @Override
    public Score one(String id) {
        Score score = baseMapper.selectById(id);
        return score;
    }

    @Override
    public boolean del(String id) {
        int  del = baseMapper.deleteById(id);
        if(del==1){
            return true;
        }
        return false;
    }

    @Override
    public void importList(MultipartFile file, Score score) throws Exception {


        ImportParams params = new ImportParams();
        params.setTitleRows(1); //标题列占几行
        params.setHeadRows(1); //列名占几行
        params.setNeedSave(true);

        List<ScoreExcel> list = ExcelImportUtil.importExcel(file.getInputStream(), ScoreExcel.class, params);

        List<Score> scoreList=new ArrayList<>();
        list.stream().forEach(scoreExcel -> {
            Score score1=new Score();
            score1.setUsername(scoreExcel.getUsername());
            score1.setChinese(scoreExcel.getChinese());
            score1.setMathematics(scoreExcel.getMathematics());
            score1.setEnglish(scoreExcel.getEnglish());
            score1.setGeneralScience(scoreExcel.getGeneralScience());

            int totalScore=scoreExcel.getChinese()==null?0:scoreExcel.getChinese()+(scoreExcel.getEnglish()==null?0:scoreExcel.getEnglish())+(scoreExcel.getMathematics()==null?0:scoreExcel.getMathematics())+(scoreExcel.getGeneralScience()==null?0:scoreExcel.getGeneralScience());
            score1.setTotalScore(totalScore);
            String semester="第一学期".equals(scoreExcel.getSemester())?"1":"第二学期".equals(scoreExcel.getSemester())?"2":null;
           score1.setSemester(semester);
            LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(User::getName,scoreExcel.getUsername());
            List<User> userList = userService.list(wrapper);
            if(userList.size()==1){
                score1.setUserId(userList.get(0).getId());
                score1.setClasss(userList.get(0).getClasss());
                score1.setGrade(userList.get(0).getGrade());
                LambdaQueryWrapper<Score> scoreLambdaQueryWrapper=new LambdaQueryWrapper<>();
                scoreLambdaQueryWrapper.eq(Score::getUserId,userList.get(0).getId());
                scoreLambdaQueryWrapper.eq(Score::getSemester,semester);
                scoreLambdaQueryWrapper.eq(Score::getGrade,userList.get(0).getGrade());
                List<Score> scores = baseMapper.selectList(scoreLambdaQueryWrapper);
                if(scores.size()==1){
                    baseMapper.deleteById(scores.get(0).getId());
                }
                scoreList.add(score1);
            }else{
                throw new RuntimeException(scoreExcel.getUsername() + "该学生不存在");
            }
        });

        scoreList.stream().forEach(score1 -> {
            baseMapper.insert(score1);
        });
//        scoreService.saveBatch(scoreList);
    }

    @Override
    public void exportList(HttpServletResponse response,Score score) throws IOException {
        List<Score> scoreList = baseMapper.selectPage(score);
        List<ScoreExpotExcel> scoreExpotExcels = BeanUtil.copyToList(scoreList, ScoreExpotExcel.class);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("成绩列表.xlx", "UTF-8"));

        ServletOutputStream out = response.getOutputStream();


        //设置excel参数
        ExportParams params = new ExportParams();
        //设置sheet名名称
        params.setSheetName("成绩列表");
        //设置标题
        params.setTitle("成绩信息表");

        params.setType(ExcelType.HSSF);

        Workbook workbook = ExcelExportUtil.exportExcel(params, ScoreExpotExcel.class, scoreExpotExcels);
        workbook.write(out);
    }

    @Override
    public List<ScoreStatistics> gradeStatistics(Score score) {
        List<ScoreStatistics> scoreStatistics = baseMapper.gradeStatistics(score);
        return scoreStatistics;
    }

    @Override
    public List<ScoreStatistics> classStatisticsList(Score score) {
        List<ScoreStatistics> scoreStatistics = baseMapper.classStatistics(score);
        return scoreStatistics;
    }
}
