package com.example.springbootdemo.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ProcessBo;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.model.Leave;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.ILeaveService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.catalina.Globals;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-04-06
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {


    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;


    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("page")
    public ResponseBo page(int pageNum,int pageSize,String val){
        IPage<Leave> page = leaveService.getPage(pageNum, pageSize,val);
        return ResponseBo.ok(page);
    }


    /**
     * 添加
     * @param leave
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加请假单")
    public ResponseBo add(@RequestBody Leave leave){
        String userId = JwtUtil.getCurrentUserId();
        leave.setUserId(userId);
        boolean save = leaveService.add(leave);
        if(save){
            return ResponseBo.ok("添加成功");
        }else{
            return ResponseBo.error("添加失败");
        }
    }


    /**
     * 编辑
     * @param leave
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改请假单")
    public ResponseBo edit(@RequestBody Leave leave){
        boolean save = leaveService.edit(leave);
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
            Leave one = leaveService.one(id);
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
    @Log(title = "删除请假单")
    public ResponseBo del(String id){
        if(StringUtils.isNotBlank(id)){
            boolean del = leaveService.del(id);
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
     * 得到所有流程图
     * @return
     */
    @GetMapping("/getProcessList")
    public ResponseBo getProcessList(){
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().latestVersion().list();
        List<ProcessBo> processBoList=new ArrayList<>();
        for (ProcessDefinition processDefinition:list){
            ProcessBo processBo=new ProcessBo();
            BeanUtils.copyProperties(processDefinition,processBo);
            processBoList.add(processBo);
        }
        return ResponseBo.ok(processBoList);
    }

    /**
     * 启动流程
     * @return
     */
    @Log(title = "提交请假单")
    @PostMapping("/startActivity")
    public ResponseBo startActivity(@RequestBody Leave leave){
        leaveService.startActivity(leave);
        return ResponseBo.ok();
    }

    /**
     * 设置审批人
     * @return
     */
    @Log(title = "设置审批人")
    @PostMapping("/setApprover")
    public ResponseBo setApprover(@RequestBody Leave leave){
        Map<String,Object> mapComplete=new HashMap<>();
        mapComplete.put("user",leave.getApprovalUserId());
        mapComplete.put("houUser",leave.getApprovalUserId());
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
        Task task = taskService.createTaskQuery().processInstanceId(processDefinition.getDeploymentId()).singleResult();
//        taskService.complete(task.getId(),mapComplete);
        Authentication.setAuthenticatedUserId(leave.getApprovalUserId());
        return ResponseBo.ok();
    }


    /**
     * 我的任务
     * @return
     */
    @RequestMapping("/getTaskList")
    public ResponseBo getTaskList(int pageNum, int pageSize){
        IPage<Leave> page=leaveService.getTaskPage(pageNum, pageSize);
        return ResponseBo.ok(page);
    }
    /**
     * 审批
     * @param leave
     * @return
     */
    @PostMapping("/complete")
    @Log(title = "提交审批")
    public ResponseBo complete(@RequestBody Leave leave){
        leaveService.complete(leave);

        return ResponseBo.ok();
    }

    /**
     * 查看流程图
     */
    @RequestMapping("/getProcessView")
    public void getProcessView(HttpServletResponse response,@RequestParam String processInstanceId) throws Exception {

        leaveService.getProcessView(processInstanceId,response);
    }

    /**
     * 审批历史
     */
    @RequestMapping("/historyApproval")
    public ResponseBo historyApproval(String processInstanceId) {
        List<Map> comments = leaveService.historyApproval(processInstanceId);
        return ResponseBo.ok(comments);
    }
}
