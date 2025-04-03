package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.Project;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-04-06
 */
public interface IProjectService extends IService<Project> {

    IPage<Project> getPage(int pageNum, int pageSize,String val);

    boolean add(Project project);

    boolean edit(Project project);

    boolean del(String id);

    Project one(String id);

    boolean startActivity(Project project);

    IPage<Project> getTaskPage(int pageNum, int pageSize,String approvalStatus);

    void complete(Project project);

    void getProcessView(String processInstanceId, HttpServletResponse response) throws Exception;

    IPage<Map> historyApproval(Integer pageNum,Integer pageSize,String processInstanceId);

    void receiveTask(String projectId,String taskId);

    void returnTask(String projectId,String taskId);


}
