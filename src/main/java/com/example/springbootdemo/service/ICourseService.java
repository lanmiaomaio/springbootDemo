package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.Course;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2025-03-19
 */
public interface ICourseService extends IService<Course> {

    IPage<Course> getPage(int pageNum,int pageSize,Course course);

    boolean add(Course course);

    boolean edit(Course course);

    Course one(String id);

    void createSubject(String classId) throws IOException;

    IPage<Course> getClassCoursePage(int pageNum,int pageSize,Course course);

    boolean del(String id);


    List<Map<String, Object>> gradeClassList(List<Course> list);

}
