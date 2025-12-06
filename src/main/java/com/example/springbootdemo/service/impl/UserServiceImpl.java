package com.example.springbootdemo.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.mapper.ScoreMapper;
import com.example.springbootdemo.mapper.UserCourseMapper;
import com.example.springbootdemo.model.*;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.model.excelVo.UserExcel;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.ICourseService;
import com.example.springbootdemo.service.IScoreUserService;
import com.example.springbootdemo.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2024-06-06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private ISysDictionaryService dictionaryService;

    @Autowired
    private IScoreUserService scoreUserService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Override
    public User findByUserName(String userName) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getName,userName);
        return baseMapper.selectOne(userLambdaQueryWrapper);
    }

    @Override
    public IPage<User> getPage(int pageNum, int pageSize, User user) {
        IPage<User> page=new Page<>(pageNum,pageSize);
        String currentUserId = JwtUtil.getCurrentUserId();
        if ("1".equals(currentUserId)) {
            IPage<User> userIPage = baseMapper.userList(page, user,null);
            return userIPage;
        }
        SysUser sysUser   = sysUserService.getById(currentUserId);
        IPage<User> userIPage=new Page<>();
        if(StringUtils.isNotBlank(sysUser.getClasss())){
            userIPage = baseMapper.userList(page, user,sysUser.getClasss().split(","));
        }
        return userIPage;
    }

    @Override
    @Transactional
    public boolean edit(User user) {
        int i = baseMapper.updateById(user);
        LambdaUpdateWrapper<UserCourse> userCourseLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        userCourseLambdaUpdateWrapper.eq(UserCourse::getGradeId,user.getGrade());
        userCourseLambdaUpdateWrapper.eq(UserCourse::getUserId,user.getId());
        Integer integer = userCourseMapper.selectCount(userCourseLambdaUpdateWrapper);
        if(integer>0){
            LambdaUpdateWrapper<UserCourse> courseLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            courseLambdaUpdateWrapper.set(UserCourse::getGradeId,user.getGrade());
            courseLambdaUpdateWrapper.set(UserCourse::getClasssId,user.getClasss());
            courseLambdaUpdateWrapper.eq(UserCourse::getUserId,user.getId());
            userCourseMapper.update(new UserCourse(),courseLambdaUpdateWrapper);
        }else{
            UserCourse userCourse=new UserCourse();
            userCourse.setUserId(user.getId());
            userCourse.setGradeId(user.getGrade());
            userCourse.setClasssId(user.getClasss());
            userCourseMapper.insert(userCourse);

        }
        if(i==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    @Transactional
    public boolean add(User user) {
        user.setStatus("be4521e9b35b81ffc52eee3b9eff01c4");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String format = sdf.format(new Date());
        String maxUserNo = baseMapper.selectMaxUserNo(format);
        user.setUserNo(format+String.format("%04d", (StringUtils.isNotBlank(maxUserNo)?Integer.parseInt(maxUserNo):0) +1));
        int insert = baseMapper.insert(user);
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean updateStatus(String id, String status) {
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.eq(User::getId,id);
        userLambdaUpdateWrapper.set(User::getStatus,status);
        int update = baseMapper.update(new User(), userLambdaUpdateWrapper);
        if(update==0){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public User one(String id) {
        User user = baseMapper.selectById(id);
        if(StringUtils.isNotBlank(user.getGrade())){
            user.setGradeName(courseService.getById(user.getGrade()).getTitle());
        }
        if(StringUtils.isNotBlank(user.getClasss())){
            user.setClassName(courseService.getById(user.getClasss()).getTitle());
        }
        return user;
    }

    @Override
    public IPage<Score> getScorePage(int pageNum, int pageSize, Score score) {
        String currentUserId = JwtUtil.getCurrentUserId();
        IPage<Score> scoreList=new Page<>();

        Page<Score> page = new Page<>(pageNum, pageSize);
        if ("1".equals(currentUserId)) {
            scoreList = scoreMapper.selectPage(page,score, null);
        }else{
            SysUser sysUser   = sysUserService.getById(currentUserId);
            if(StringUtils.isNotBlank(sysUser.getClasss())){
                scoreList = scoreMapper.selectPage(page,score,StringUtils.isNotBlank(sysUser.getClasss())?sysUser.getClasss().split(","):null);

            }
        }
        return scoreList;
    }

    @Override
    public boolean del(String id) {
        LambdaQueryWrapper<ScoreUser> scoreUserLambdaQueryWrapper=new LambdaQueryWrapper<>();
        scoreUserLambdaQueryWrapper.eq(ScoreUser::getUserId,id);
        List<ScoreUser> list = scoreUserService.list(scoreUserLambdaQueryWrapper);
        if(list.size()>0){
            throw new RuntimeException("该学生绑定成绩，无法删除！");
        }
        int i = baseMapper.deleteById(id);
        if(i==0){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void exportList(HttpServletResponse response, User user) throws IOException {
        List<User> userList=new ArrayList<>();
        String currentUserId = JwtUtil.getCurrentUserId();
        if ("1".equals(currentUserId)) {
            userList = baseMapper.userList(user,null);
        }else{
            SysUser sysUser   = sysUserService.getById(currentUserId);
            if(StringUtils.isNotBlank(sysUser.getClasss())){
                userList = baseMapper.userList(user,sysUser.getClasss().split(","));
            }
        }

        List<UserExcel> userExcelList = BeanUtil.copyToList(userList, UserExcel.class);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("用户列表.xlx", "UTF-8"));

        ServletOutputStream out = response.getOutputStream();


        //设置excel参数
        ExportParams params = new ExportParams();
        //设置sheet名名称
        params.setSheetName("用户列表");
        //设置标题
        params.setTitle("用户信息表");

        params.setType(ExcelType.HSSF);

        Workbook workbook = ExcelExportUtil.exportExcel(params, UserExcel.class, userExcelList);
        workbook.write(out);
    }

    @Override
    public void importList(MultipartFile file) throws Exception {

        ImportParams params = new ImportParams();
        params.setTitleRows(1); //标题列占几行
        params.setHeadRows(1); //列名占几行
        params.setNeedSave(true);

        List<UserExcel> list = ExcelImportUtil.importExcel(file.getInputStream(), UserExcel.class, params);

        List<User> userArrayList=new ArrayList<>();
        list.stream().forEach(userExcel -> {
            User user=new User();
            user.setName(userExcel.getName());
            LambdaQueryWrapper<Course> gradeQueryWrapper=new LambdaQueryWrapper<>();
            gradeQueryWrapper.eq(Course::getTitle,userExcel.getGradeName()).eq(Course::getStatus,"1");
            List<Course> courseList = courseService.list(gradeQueryWrapper);
            if(courseList.size()==1){
                user.setGrade(courseList.get(0).getId());
            }else{
                throw new RuntimeException(userExcel.getGradeName() + "不存在");
            }

            LambdaQueryWrapper<Course> classQueryWrapper=new LambdaQueryWrapper<>();
            classQueryWrapper.eq(Course::getTitle,userExcel.getClassName()).eq(Course::getStatus,"1");
            classQueryWrapper.eq(Course::getParentId,courseList.get(0).getId());
            List<Course> courseClassList = courseService.list(classQueryWrapper);
            if(courseClassList.size()==1){
                user.setClasss(courseClassList.get(0).getId());

            }else{
                throw new RuntimeException(userExcel.getGradeName()+" "+userExcel.getClassName() + "不在");

            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String format = sdf.format(new Date());
            String maxUserNo = baseMapper.selectMaxUserNo(format);
            user.setUserNo(format+String.format("%04d", maxUserNo==null?1:Integer.parseInt(maxUserNo) +1));
            user.setAge(userExcel.getAge());
            user.setPhone(userExcel.getPhone());
            user.setGender(userExcel.getGender());
            user.setBirthday(userExcel.getBirthday());
            user.setAddress(userExcel.getAddress());
            user.setStatus("be4521e9b35b81ffc52eee3b9eff01c4");
            userArrayList.add(user);
        });

        userArrayList.stream().forEach(user -> {
            baseMapper.insert(user);
            UserCourse userCourse=new UserCourse();
            userCourse.setUserId(user.getId());
            userCourse.setGradeId(user.getGrade());
            userCourse.setClasssId(user.getClasss());
            userCourseMapper.insert(userCourse);

        });
    }

    @Override
    public List<Map> userStatistics(String[] classNameArray) {
        return baseMapper.userStatistics(classNameArray);
    }
}
