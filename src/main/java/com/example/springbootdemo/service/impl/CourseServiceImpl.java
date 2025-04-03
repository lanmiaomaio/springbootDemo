package com.example.springbootdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.model.Course;
import com.example.springbootdemo.mapper.CourseMapper;
import com.example.springbootdemo.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2025-03-19
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private ISysDictionaryService sysDictionaryService;

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public IPage<Course> getPage(int pageNum, int pageSize, Course course) {
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.isNull(Course::getParentId).orderByDesc(Course::getCreateTime);
        Page<Course> page=new Page<>(pageNum,pageSize);
        IPage<Course> courseIPage = baseMapper.selectPage(page, courseLambdaQueryWrapper);
        courseIPage.getRecords().stream().forEach(course1 -> {
            course1.setCreateBy(sysUserService.getById(course1.getCreateBy()).getRealName());
        });
        return courseIPage;
    }

    @Override
    public boolean add(Course course) {
        course.setCreateBy(JwtUtil.getCurrentUserId());
        int insert = baseMapper.insert(course);
        if(course.getClassList()!=null){
            course.getClassList().stream().forEach(classs->{
                classs.setParentId(course.getId());
                classs.setCreateBy(JwtUtil.getCurrentUserId());
                baseMapper.insert(classs);
            });
        }
        if(course.getCourseList()!=null){
            course.getCourseList().stream().forEach(course1->{
                course1.setParentId(course.getId());
                course1.setCreateBy(JwtUtil.getCurrentUserId());
                baseMapper.insert(course1);
            });
        }
        if(course.getScoreCourseList()!=null){
            course.getScoreCourseList().stream().forEach(scoreCourse->{
                scoreCourse.setParentId(course.getId());
                scoreCourse.setCreateBy(JwtUtil.getCurrentUserId());
                baseMapper.insert(scoreCourse);
            });
        }

        if(insert==0){
            return false;
        }
        return true;
    }

    @Override
    public boolean edit(Course course) {
        course.setUpdateBy(JwtUtil.getCurrentUserId());
        int update = baseMapper.updateById(course);
        LambdaQueryWrapper<Course> classLambdaQueryWrapper=new LambdaQueryWrapper<>();
        classLambdaQueryWrapper.eq(Course::getParentId,course.getId()).eq(Course::getType,"class");
        List<Course> classList = baseMapper.selectList(classLambdaQueryWrapper);
        if(classList.size()>0){
            baseMapper.deleteBatchIds(classList.stream().map(Course::getId).collect(Collectors.toList()));
        }

        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getParentId,course.getId()).eq(Course::getType,"course");
        List<Course> courseList = baseMapper.selectList(courseLambdaQueryWrapper);

        if(courseList.size()>0){
            baseMapper.deleteBatchIds(courseList.stream().map(Course::getId).collect(Collectors.toList()));
        }

        LambdaQueryWrapper<Course> scoreCourseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        scoreCourseLambdaQueryWrapper.eq(Course::getParentId,course.getId()).eq(Course::getType,"scoreCourse");
        List<Course> scoreCourseList = baseMapper.selectList(scoreCourseLambdaQueryWrapper);

        if(scoreCourseList.size()>0){
            baseMapper.deleteBatchIds(scoreCourseList.stream().map(Course::getId).collect(Collectors.toList()));
        }

        if(course.getClassList()!=null){
            course.getClassList().stream().forEach(classs->{
                classs.setParentId(course.getId());
                classs.setCreateBy(JwtUtil.getCurrentUserId());
                baseMapper.insert(classs);
            });
        }
        if(course.getCourseList()!=null){
            course.getCourseList().stream().forEach(course1->{
                course1.setParentId(course.getId());
                course1.setCreateBy(JwtUtil.getCurrentUserId());
                baseMapper.insert(course1);
            });
        }
        if(course.getScoreCourseList()!=null){
            course.getScoreCourseList().stream().forEach(scoreCourse->{
                scoreCourse.setParentId(course.getId());
                scoreCourse.setCreateBy(JwtUtil.getCurrentUserId());
                baseMapper.insert(scoreCourse);
            });
        }
        if(update==0){
            return false;
        }
        return true;
    }

    @Override
    public Course one(String id) {
        Course course = baseMapper.selectById(id);
        LambdaQueryWrapper<Course> classLambdaQueryWrapper=new LambdaQueryWrapper<>();
        classLambdaQueryWrapper.eq(Course::getParentId,id).eq(Course::getType,"class").orderByAsc(Course::getSortBy);
        List<Course> classList = baseMapper.selectList(classLambdaQueryWrapper);
        course.setClassList(classList);

        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getParentId,id).eq(Course::getType,"course").orderByAsc(Course::getSortBy);
        List<Course> courseList = baseMapper.selectList(courseLambdaQueryWrapper);
        course.setCourseList(courseList);

        LambdaQueryWrapper<Course> scoreCourseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        scoreCourseLambdaQueryWrapper.eq(Course::getParentId,id).eq(Course::getType,"scoreCourse").orderByAsc(Course::getSortBy);
        List<Course> scoreCourseList = baseMapper.selectList(scoreCourseLambdaQueryWrapper);
        course.setScoreCourseList(scoreCourseList);
        return course;
    }

    @Override
    public IPage<Course> getClassCoursePage(int pageNum,int pageSize,Course course) {

        IPage<Course> page=new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getParentId,course.getId()).eq(Course::getType,course.getType()).orderByAsc(Course::getSortBy);
        IPage<Course> courseIPage = baseMapper.selectPage(page, courseLambdaQueryWrapper);
        courseIPage.getRecords().stream().forEach(course1 -> {
            if(StringUtils.isNotBlank(course1.getCreateBy())){
                course1.setCreateBy(sysUserService.getById(course1.getCreateBy()).getRealName());
            }
        });
        return courseIPage;
    }

    @Override
    public boolean del(String id) {
        int delete = baseMapper.deleteById(id);
        LambdaQueryWrapper<Course> classLambdaQueryWrapper=new LambdaQueryWrapper<>();
        classLambdaQueryWrapper.eq(Course::getType,"class").eq(Course::getParentId,id);
        baseMapper.delete(classLambdaQueryWrapper);

        LambdaQueryWrapper<Course> courseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getType,"course").eq(Course::getParentId,id);
        baseMapper.delete(courseLambdaQueryWrapper);

        LambdaQueryWrapper<Course> scoreCourseLambdaQueryWrapper=new LambdaQueryWrapper<>();
        scoreCourseLambdaQueryWrapper.eq(Course::getType,"scoreCourse").eq(Course::getParentId,id);
        baseMapper.delete(scoreCourseLambdaQueryWrapper);
        if(delete==0){
            return false;
        }
        return true;
    }


    @Override
    public List<Map<String, Object>> gradeClassList(List<Course> list) {
        List<Map<String,Object>> mapList=new ArrayList<>();
        list.stream().forEach(course -> {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("label", course.getTitle());
            objectMap.put("value", course.getId());
            objectMap.put("code", course.getType());
            objectMap.put("parentId", course.getParentId());
            objectMap.put("scienceHumanitiesClass", course.getScienceHumanitiesClass());
            objectMap.put("parentName", "");
            mapList.add(objectMap);
        });
        List<Map<String, Object>> mapList1 = childDictionary(mapList);
        return mapList1;
    }

    public List<Map<String, Object>> childDictionary(List<Map<String,Object>> list){
        list.stream().forEach(map -> {
            LambdaQueryWrapper<Course> courseLambdaQueryWrapper = new LambdaQueryWrapper<>();
            courseLambdaQueryWrapper.eq(Course::getParentId,map.get("value")).eq(Course::getType,"class").eq(Course::getStatus,"1").orderByAsc(Course::getSortBy);
            List<Course> courseList = baseMapper.selectList(courseLambdaQueryWrapper);
            List<Map<String,Object>> mapList1=new ArrayList<>();
            courseList.stream().forEach(course -> {

                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("label", course.getTitle());
                objectMap.put("value", course.getId());
                objectMap.put("code", course.getType());
                objectMap.put("parentId", course.getParentId());
                objectMap.put("parentName", map.get("label"));
                objectMap.put("scienceHumanitiesClass", course.getScienceHumanitiesClass());
                mapList1.add(objectMap);
            });
            if(courseList.size()!=0){
                map.put("children",mapList1);
            }
            childDictionary(mapList1);

        });
        return list;
    }
}
