package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.Leave;
import com.baomidou.mybatisplus.extension.service.IService;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-04-06
 */
public interface ILeaveService extends IService<Leave> {

    IPage<Leave> getPage(int pageNum, int pageSize,String val);

    boolean add(Leave leave);

    boolean edit(Leave leave);

    boolean del(String id);

    Leave one(String id);

    boolean startActivity(Leave leave);

    IPage<Leave> getTaskPage(int pageNum, int pageSize);

    void complete(Leave leave);

    void getProcessView(String processInstanceId, HttpServletResponse response) throws Exception;

    List<Map> historyApproval(String processInstanceId);

}
