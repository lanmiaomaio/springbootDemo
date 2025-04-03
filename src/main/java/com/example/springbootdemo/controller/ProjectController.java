package com.example.springbootdemo.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Project;
import com.example.springbootdemo.model.system.ProcessBo;
import com.example.springbootdemo.service.IProjectService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liya test
 * @since 2023-04-06
 */
@RestController
@RequestMapping("/project")
public class ProjectController {


    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;


    @Autowired
    private IProjectService projectService;

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
        IPage<Project> page = projectService.getPage(pageNum, pageSize,val);
        return ResponseBo.ok(page);
    }


    /**
     * 添加
     * @param project
     * @return
     */
    @PostMapping("/add")
    @Log(title = "添加项目单")
    @ButtonPermission(perm = "project:add")
    public ResponseBo add(@RequestBody Project project){
        String userId = JwtUtil.getCurrentUserId();
        project.setUserId(userId);
        boolean save = projectService.add(project);
        if(save){
            return ResponseBo.ok("添加成功");
        }else{
            return ResponseBo.error("添加失败");
        }
    }


    /**
     * 编辑
     * @param project
     * @return
     */
    @PostMapping("/edit")
    @Log(title = "修改项目单")
    @ButtonPermission(perm = "project:edit")
    public ResponseBo edit(@RequestBody Project project){
        boolean save = projectService.edit(project);
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
    @ButtonPermission(perm = "project:view")
    public ResponseBo one(String id){
        if(StringUtils.isNotBlank(id)){
            Project one = projectService.one(id);
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
    @Log(title = "删除项目单")
    @ButtonPermission(perm = "leave:del")
    public ResponseBo del(String id){
        if(StringUtils.isNotBlank(id)){
            boolean del = projectService.del(id);
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
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionCategory("c1207474e3de103261903e02d0ee5e28").active().latestVersion().list();
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
    @Log(title = "提交项目单")
    @PostMapping("/startActivity")
    @ButtonPermission(perm = "project:subApprove")
    public ResponseBo startActivity(@RequestBody Project project){
        projectService.startActivity(project);
        return ResponseBo.ok();
    }

    /**
     * 项目管理-审批历史
     */
    @RequestMapping("/historyApproval")
    @ButtonPermission(perm = "project:historyApproval")
    public ResponseBo leaveHistoryApproval(Integer pageNum,Integer pageSize,String processInstanceId) {
        IPage<Map> comments = projectService.historyApproval(pageNum,pageSize,processInstanceId);
        return ResponseBo.ok(comments);
    }



    /**
     * 我的任务
     * @return
     */
    @RequestMapping("/getTaskList")
    public ResponseBo getTaskList(int pageNum, int pageSize,String approvalStatus){
        IPage<Project> page=projectService.getTaskPage(pageNum, pageSize,approvalStatus);
        return ResponseBo.ok(page);
    }
    /**
     * 审批
     * @param project
     * @return
     */
    @PostMapping("/complete")
    @Log(title = "审批")
    @ButtonPermission(perm = "project:approve:approve")
    public ResponseBo complete(@RequestBody Project project){
        projectService.complete(project);

        return ResponseBo.ok();
    }

    /**
     * 设置审批人
     * @return
     */
    @Log(title = "指派审批人")
    @GetMapping("/setApprover")
    @ButtonPermission(perm = "project:approve:assign")
    public ResponseBo setApprover(String projectId, String taskId,String approvalUserId){

        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason","");
        jsonObject.put("examineStatus","6");
        jsonObject.put("approvalUserId",approvalUserId);
        Project project = projectService.getById(projectId);
        taskService.addComment(taskId,project.getProcessInstanceId(),"项目管理指派审批人",jsonObject.toJSONString());

        taskService.setAssignee(taskId,approvalUserId);

        return ResponseBo.ok();
    }

    /**
     * 查看流程图
     */
    @RequestMapping("/getProcessView")
    public void getProcessView(HttpServletResponse response,@RequestParam String processInstanceId) throws Exception {

        projectService.getProcessView(processInstanceId,response);
    }



    /**
     * 我的审批-审批历史
     */
    @RequestMapping("/approvalHistoryApproval")
    @ButtonPermission(perm = "project:approve:historyApproval")
    public ResponseBo approvalHistoryApproval(Integer pageNum,Integer pageSize,String processInstanceId) {
        IPage<Map> comments = projectService.historyApproval(pageNum,pageSize,processInstanceId);
        return ResponseBo.ok(comments);
    }

    /**
     * 领取任务
     */
    @RequestMapping("/receiveTask")
    @Log(title = "领取任务")
    @ButtonPermission(perm = "project:approve:receive")
    public ResponseBo receiveTask(String projectId,String taskId) {
        projectService.receiveTask(projectId,taskId);
        return ResponseBo.ok();
    }


    /**
     * 领取任务
     */
    @RequestMapping("/returnTask")
    @Log(title = "退回任务")
    @ButtonPermission(perm = "project:approve:return")
    public ResponseBo returnTask(String projectId,String taskId) {
        projectService.returnTask(projectId,taskId);
        return ResponseBo.ok();
    }

    /**
     * 项目审批详情
     * @param id
     * @return
     */
    @GetMapping("/approvalOne")
    @ButtonPermission(perm = "project:approve:view")
    public ResponseBo approvalOne(String id){
        if(StringUtils.isNotBlank(id)){
            Project one = projectService.one(id);
            return ResponseBo.ok(one);
        }else{
            return ResponseBo.error("查询失败");
        }
    }
}
