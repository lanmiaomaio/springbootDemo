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
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.model.*;
import com.example.springbootdemo.mapper.ScoreMapper;
import com.example.springbootdemo.model.excelVo.ScoreExcel;
import com.example.springbootdemo.model.excelVo.ScoreExpotExcel;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

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

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ISysDictionaryService sysDictionaryService;

    @Autowired
    private IScoreUserService scoreUserService;

    @Autowired
    private IScoreCategoryService scoreCategoryService;

    @Autowired
    private IScoreScoreCategoryService scoreScoreCategoryService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IScoreCourseService scoreCourseService;




    @Override
    public IPage<ScoreCategory> getPage(int pageNum, int pageSize, Score score) {
        String currentUserId = JwtUtil.getCurrentUserId();
        IPage<ScoreCategory> scoreList=new Page<>();
        Page<ScoreCategory> page = new Page<>(pageNum, pageSize);
        if ("1".equals(currentUserId)) {
            scoreList = baseMapper.selectScoreCategoryPage(page,score, null);
        }else{
            SysUser sysUser   = sysUserService.getById(currentUserId);
            if(StringUtils.isNotBlank(sysUser.getClasss())){
                scoreList = baseMapper.selectScoreCategoryPage(page,score,StringUtils.isNotBlank(sysUser.getClasss())?sysUser.getClasss().split(","):null);

            }
        }
        scoreList.setRecords(getChildMenu(scoreList.getRecords(),score));
        return scoreList;
    }


    public List<ScoreCategory> getChildMenu(List<ScoreCategory> scoreCategoryList,Score score){
        scoreCategoryList.stream().forEach(scoreCategory -> {
            score.setScoreCategoryId(scoreCategory.getId());
//            LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
//            courseLambdaQueryWrapper.eq(Course::getType,"scoreCourse").eq(Course::getStatus,"1");
//            List<Course> courseList = courseService.list(courseLambdaQueryWrapper);
//            List<Course> scoreCourseList=courseList.stream().collect(
//                    Collectors.collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(Course::getCode))),
//                            ArrayList::new));
//            score.setScoreCourseList(scoreCourseList);

            List<Score> scoreList = baseMapper.selectPage(score, null);
            if(scoreList.size()!=0){
                scoreCategory.setChildren(scoreList);
//                getChildMenu(scoreCategoryList,score);
            }

        });
        return scoreCategoryList;
    }

    @Override
    public List<User> getList() {
        String currentUserId = JwtUtil.getCurrentUserId();
        SysUser sysUser = sysUserService.getById(currentUserId);
        User user=new User();
        user.setStatus("be4521e9b35b81ffc52eee3b9eff01c4");
        if ("1".equals(currentUserId)) {
            List<User> list = userMapper.userList(user,null);
            return list;
        }
        List<User> list=new ArrayList<>();
        if(StringUtils.isNotBlank(sysUser.getClasss())){
            list = userMapper.userList(user,sysUser.getClasss().split(","));

        }
        return list;


    }

    @Override
    public int add(Score score) {
        User user = userService.getById(score.getUserId());

        int totalScore=(score.getChinese()==null?0:score.getChinese())+(score.getEnglish()==null?0:score.getEnglish())+(score.getMathematics()==null?0:score.getMathematics())+(score.getScience()==null?0:score.getScience())+
                (score.getPhysics()==null?0:score.getPhysics())+(score.getChemistry()==null?0:score.getChemistry())+(score.getOrganism()==null?0:score.getOrganism())+(score.getHistory()==null?0:score.getHistory())+(score.getPolitics()==null?0:score.getPolitics())+(score.getGeography()==null?0:score.getGeography())+(score.getHumanities()==null?0:score.getHumanities());
        score.setTotalScore(totalScore);
        int insert = baseMapper.insert(score);
        ScoreUser scoreUser=new ScoreUser();
        scoreUser.setUserId(score.getUserId());
        scoreUser.setClasss(user.getClasss());
        scoreUser.setGrade(user.getGrade());
        return insert;
    }

    @Override
    public boolean edit(Score score) {
        int totalScore=(score.getChinese()==null?0:score.getChinese())+(score.getEnglish()==null?0:score.getEnglish())+(score.getMathematics()==null?0:score.getMathematics())+(score.getScience()==null?0:score.getScience())+
                (score.getPhysics()==null?0:score.getPhysics())+(score.getChemistry()==null?0:score.getChemistry())+(score.getOrganism()==null?0:score.getOrganism())+(score.getHistory()==null?0:score.getHistory())+(score.getPolitics()==null?0:score.getPolitics())+(score.getGeography()==null?0:score.getGeography())+(score.getHumanities()==null?0:score.getHumanities());
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
        if (StringUtils.isNotBlank(score.getGrade())) {
            SysDictionary sysDictionary = sysDictionaryService.getById(score.getGrade());
            score.setGradeName(sysDictionary.getName());
        }
        return score;
    }

    @Override
    @Transactional
    public boolean del(String id,String type) {
        if("1".equals(type)){
            LambdaQueryWrapper<ScoreScoreCategory> scoreScoreCategoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
            scoreScoreCategoryLambdaQueryWrapper.eq(ScoreScoreCategory::getScoreCategoryId,id);
            List<ScoreScoreCategory> scoreScoreCategoryList = scoreScoreCategoryService.list(scoreScoreCategoryLambdaQueryWrapper);
            baseMapper.deleteBatchIds(scoreScoreCategoryList.stream().map(ScoreScoreCategory::getScoreId).collect(Collectors.toList()));
            List<ScoreUser> scoreUserList=new ArrayList<>();
            scoreScoreCategoryList.stream().forEach(scoreScoreCategory -> {
                LambdaQueryWrapper<ScoreUser> scoreUserLambdaQueryWrapper=new LambdaQueryWrapper<>();
                scoreUserLambdaQueryWrapper.eq(ScoreUser::getScoreId,scoreScoreCategory.getScoreId());
                scoreUserList.addAll(scoreUserService.list(scoreUserLambdaQueryWrapper));


            });
            scoreScoreCategoryService.removeByIds(scoreScoreCategoryList.stream().map(ScoreScoreCategory::getId).collect(Collectors.toList()));

            scoreUserService.removeByIds(scoreUserList.stream().map(ScoreUser::getId).collect(Collectors.toList()));
            scoreCategoryService.removeById(id);
            return true;
        }else{
            int  del = baseMapper.deleteById(id);

            LambdaQueryWrapper<ScoreUser> scoreUserLambdaQueryWrapper=new LambdaQueryWrapper<>();
            scoreUserLambdaQueryWrapper.eq(ScoreUser::getScoreId,id);
            ScoreUser scoreUser = scoreUserService.getOne(scoreUserLambdaQueryWrapper);
            scoreUserService.removeById(scoreUser.getId());

            LambdaQueryWrapper<ScoreScoreCategory> scoreScoreCategoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
            scoreScoreCategoryLambdaQueryWrapper.eq(ScoreScoreCategory::getScoreId,id);
            ScoreScoreCategory scoreScoreCategory = scoreScoreCategoryService.getOne(scoreScoreCategoryLambdaQueryWrapper);
            scoreScoreCategoryService.removeById(scoreScoreCategory.getId());

            if(del==1){
                return true;
            }
            return false;
        }

    }



    public void importList1(MultipartFile file, Score score) throws Exception {


        ImportParams params = new ImportParams();
        params.setTitleRows(1); //标题列占几行
        params.setHeadRows(1); //列名占几行
        params.setNeedSave(true);

        List<Map> list = ExcelImportUtil.importExcel(file.getInputStream(), Map.class, params);
        List<Score> scoreList=new ArrayList<>();
        list.stream().forEach(map -> {
            Set<String> keySet=map.keySet();
            Score score1=new Score();
            BeanUtils.copyProperties(map,score1);
            List<ScoreCourse> scoreCourseList=new ArrayList<>();
            int i=0;
            int totalScore=0;
            for (String key:keySet){
                i++;
                if(i>2){
                    LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    courseLambdaQueryWrapper.eq(Course::getParentId,score.getGrade()).eq(Course::getTitle,key).eq(Course::getType,"scoreCourse").eq(Course::getStatus,"1");
                    List<Course> courseList = courseService.list(courseLambdaQueryWrapper);
                    if(courseList.size()>0){
                        ScoreCourse scoreCourse=new ScoreCourse();
                        scoreCourse.setCourseId(courseList.get(0).getId());
                        scoreCourse.setScore(Integer.parseInt(map.get(key).toString()));
                        totalScore+=Integer.parseInt(map.get(key).toString());
                        scoreCourseList.add(scoreCourse);
                    }else{

                    }

                }
            }
            score1.setTotalScore(totalScore);
            score1.setScoreCourseImportList(scoreCourseList);
            List<String> keyList = new ArrayList(keySet);
            LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(User::getUserNo,map.get(keyList.get(0)));
            wrapper.eq(User::getName,map.get(keyList.get(1)));
            wrapper.eq(User::getClasss,score.getClasss());
            wrapper.eq(User::getStatus,"be4521e9b35b81ffc52eee3b9eff01c4");
            List<User> userList = userService.list(wrapper);
            if(userList.size()==1){
                score1.setUserNo(map.get(keyList.get(0)).toString());
                score1.setUsername(map.get(keyList.get(1)).toString());
                score1.setUserId(userList.get(0).getId());
                score1.setClasss(userList.get(0).getClasss());
                score1.setGrade(userList.get(0).getGrade());
                scoreList.add(score1);
            }else{
                Course gradeDictionary = courseService.getById(score.getGrade());
                Course classDictionary = courseService.getById(score.getClasss());
                throw new RuntimeException(gradeDictionary.getTitle()+classDictionary.getTitle()+"学号为:"+map.get(keyList.get(0))+"，姓名为:"+map.get(keyList.get(1)) + "学生不存在");
            }
        });

        ScoreCategory scoreCategory=new ScoreCategory();
        scoreCategory.setUserId(JwtUtil.getCurrentUserId());
        scoreCategory.setScoreCategory(score.getScoreCategory());
        scoreCategory.setSemester(score.getSemester());
        scoreCategory.setScoreTime(score.getScoreTime());
        scoreCategoryService.save(scoreCategory);

        scoreList.stream().forEach(score1 -> {
            baseMapper.insert(score1);

            score1.getScoreCourseImportList().forEach(scoreCourse -> {
                scoreCourse.setScoreId(score1.getId());
                scoreCourseService.save(scoreCourse);
            });

            ScoreUser scoreUser=new ScoreUser();
            scoreUser.setGrade(score1.getGrade());
            scoreUser.setClasss(score1.getClasss());
            scoreUser.setScoreId(score1.getId());
            scoreUser.setUserId(score1.getUserId());
            scoreUser.setRealName(score1.getUsername());
            scoreUser.setUserNo(score1.getUserNo());
            scoreUserService.save(scoreUser);

            ScoreScoreCategory scoreScoreCategory=new ScoreScoreCategory();
            scoreScoreCategory.setScoreId(score1.getId());
            scoreScoreCategory.setScoreCategoryId(scoreCategory.getId());
            scoreScoreCategoryService.save(scoreScoreCategory);
        });
//        scoreService.saveBatch(scoreList);
    }



    @Override
    @Transactional

    public void importList(MultipartFile file, Score score) throws Exception {


        ImportParams params = new ImportParams();
        params.setTitleRows(1); //标题列占几行
        params.setHeadRows(1); //列名占几行
        params.setNeedSave(true);

        List<ScoreExcel> list = ExcelImportUtil.importExcel(file.getInputStream(), ScoreExcel.class, params);
        List<Score> scoreList=new ArrayList<>();
        list.stream().forEach(scoreExcel -> {

            Score score1=new Score();
            BeanUtils.copyProperties(scoreExcel,score1);
//            int totalScore=scoreExcel.getChinese()==null?0:scoreExcel.getChinese()+(scoreExcel.getEnglish()==null?0:scoreExcel.getEnglish())+(scoreExcel.getMathematics()==null?0:scoreExcel.getMathematics())+(scoreExcel.getGeneralScience()==null?0:scoreExcel.getGeneralScience());
            int totalScore=(scoreExcel.getChinese()==null?0:scoreExcel.getChinese())+(scoreExcel.getEnglish()==null?0:scoreExcel.getEnglish())+(scoreExcel.getMathematics()==null?0:scoreExcel.getMathematics())+
                    (scoreExcel.getPhysics()==null?0:scoreExcel.getPhysics())+(scoreExcel.getChemistry()==null?0:scoreExcel.getChemistry())+(scoreExcel.getOrganism()==null?0:scoreExcel.getOrganism())+
                    (scoreExcel.getHistory()==null?0:scoreExcel.getHistory())+(scoreExcel.getPolitics()==null?0:scoreExcel.getPolitics())+(scoreExcel.getGeography()==null?0:scoreExcel.getGeography());
            score1.setTotalScore(totalScore);

            LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(User::getUserNo,scoreExcel.getUserNo());
            wrapper.eq(User::getName,scoreExcel.getUsername());
            wrapper.eq(User::getClasss,score.getClasss());
            wrapper.eq(User::getStatus,"be4521e9b35b81ffc52eee3b9eff01c4");
            List<User> userList = userService.list(wrapper);
            if(userList.size()==1){
                score1.setUserNo(scoreExcel.getUserNo());
                score1.setUsername(scoreExcel.getUsername());
                score1.setUserId(userList.get(0).getId());
                score1.setClasss(userList.get(0).getClasss());
                score1.setGrade(userList.get(0).getGrade());
                scoreList.add(score1);
            }else{
                Course gradeDictionary = courseService.getById(score.getGrade());
                Course classDictionary = courseService.getById(score.getClasss());
                throw new RuntimeException(gradeDictionary.getTitle()+classDictionary.getTitle()+"学号为:"+scoreExcel.getUserNo()+"，姓名为:"+scoreExcel.getUsername() + "学生不存在");
            }
        });

        ScoreCategory scoreCategory=new ScoreCategory();
        scoreCategory.setUserId(JwtUtil.getCurrentUserId());
        scoreCategory.setScoreCategory(score.getScoreCategory());
        scoreCategory.setSemester(score.getSemester());
        scoreCategory.setScoreTime(score.getScoreTime());
        scoreCategoryService.save(scoreCategory);

        scoreList.stream().forEach(score1 -> {
            baseMapper.insert(score1);

            ScoreUser scoreUser=new ScoreUser();
            scoreUser.setGrade(score1.getGrade());
            scoreUser.setClasss(score1.getClasss());
            scoreUser.setScoreId(score1.getId());
            scoreUser.setUserId(score1.getUserId());
            scoreUser.setRealName(score1.getUsername());
            scoreUser.setUserNo(score1.getUserNo());
            scoreUserService.save(scoreUser);

            ScoreScoreCategory scoreScoreCategory=new ScoreScoreCategory();
            scoreScoreCategory.setScoreId(score1.getId());
            scoreScoreCategory.setScoreCategoryId(scoreCategory.getId());
            scoreScoreCategoryService.save(scoreScoreCategory);
        });
//        scoreService.saveBatch(scoreList);
    }

    @Override
    public void exportList(HttpServletResponse response,Score score) throws IOException {
        List<ScoreExpotExcel> scoreExpotExcels=null;
        String currentUserId = JwtUtil.getCurrentUserId();
        if ("1".equals(currentUserId)) {
            List<Score> scoreList = baseMapper.selectPage(score,null);
            scoreExpotExcels = BeanUtil.copyToList(scoreList, ScoreExpotExcel.class);
        }else{
            SysUser sysUser   = sysUserService.getById(currentUserId);
            List<Score> scoreList=new ArrayList<>();
            if(StringUtils.isNotBlank(sysUser.getClasss())){
                scoreList = baseMapper.selectPage(score,sysUser.getClasss().split(","));

            }
            scoreExpotExcels = BeanUtil.copyToList(scoreList, ScoreExpotExcel.class);
        }

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
        String currentUserId = JwtUtil.getCurrentUserId();
        LambdaQueryWrapper<SysDictionary> sysDictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        sysDictionaryLambdaQueryWrapper.eq(SysDictionary::getCode,"score_category").orderByAsc(SysDictionary::getSortBy);
        List<SysDictionary> scoreCategoryList = sysDictionaryService.list(sysDictionaryLambdaQueryWrapper);

        score.setScoreCategoryList(scoreCategoryList);
        if ("1".equals(currentUserId)) {
            List<ScoreStatistics> scoreStatistics = baseMapper.gradeStatistics1(score,null);
            return scoreStatistics;
        }
        SysUser sysUser   = sysUserService.getById(currentUserId);
        List<ScoreStatistics> scoreStatistics=new ArrayList<>();
        if(StringUtils.isNotBlank(sysUser.getClasss())){
             scoreStatistics = baseMapper.gradeStatistics1(score,sysUser.getClasss().split(","));

        }
        return scoreStatistics;
    }

    @Override
    public List<ScoreStatistics> classStatisticsList(Score score) {
        String currentUserId = JwtUtil.getCurrentUserId();
        LambdaQueryWrapper<SysDictionary> sysDictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        sysDictionaryLambdaQueryWrapper.eq(SysDictionary::getCode,"score_category").orderByAsc(SysDictionary::getSortBy);
        List<SysDictionary> scoreCategoryList = sysDictionaryService.list(sysDictionaryLambdaQueryWrapper);

        score.setScoreCategoryList(scoreCategoryList);
        if ("1".equals(currentUserId)){
            List<ScoreStatistics> scoreStatistics = baseMapper.classStatistics(score,null);
            return scoreStatistics;
        }
        SysUser sysUser   = sysUserService.getById(currentUserId);
        List<ScoreStatistics> scoreStatistics=new ArrayList<>();
        if(StringUtils.isNotBlank(sysUser.getClasss())){
            scoreStatistics = baseMapper.classStatistics(score,sysUser.getClasss().split(","));

        }
        return scoreStatistics;
    }

    @Override
    public List<ScoreStatistics> classDetailStatisticsList(Score score) {
        String currentUserId = JwtUtil.getCurrentUserId();
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getType,"scoreCourse").eq(Course::getStatus,"1").orderByAsc(Course::getSortBy);
        List<Course> courseList = courseService.list(courseLambdaQueryWrapper);
        List<Course> distinctCourseList=courseList.stream().collect(
                Collectors.collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(Course::getCode))),
                        ArrayList::new));

        score.setScoreCourseList(distinctCourseList);
        if ("1".equals(currentUserId)){
            List<ScoreStatistics> scoreStatistics = baseMapper.classDetailStatistics(score,null);
            return scoreStatistics;
        }
        SysUser sysUser   = sysUserService.getById(currentUserId);
        List<ScoreStatistics> scoreStatistics=new ArrayList<>();
        if(StringUtils.isNotBlank(sysUser.getClasss())){
            scoreStatistics = baseMapper.classDetailStatistics(score,sysUser.getClasss().split(","));

        }
        return scoreStatistics;
    }

    @Override
    public IPage<Score> selectStatisticsDetailPage(int pageNum, int pageSize, Score score) {
        Page<Score> page = new Page<>(pageNum, pageSize);
        String currentUserId = JwtUtil.getCurrentUserId();
        if ("1".equals(currentUserId)){
            IPage<Score> scoreIPage = baseMapper.selectStatisticsDetail(page, score,null);
            return scoreIPage;
        }
        SysUser sysUser   = sysUserService.getById(currentUserId);
        IPage<Score> scoreIPage=new Page<>();
        if(StringUtils.isNotBlank(sysUser.getClasss())){
            scoreIPage = baseMapper.selectStatisticsDetail(page, score,StringUtils.isNotBlank(sysUser.getClasss())?sysUser.getClasss().split(","):null);

        }
        return scoreIPage;
    }
}
