package com.example.springbootdemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.mapper.ProjectMapper;
import com.example.springbootdemo.mapper.system.SysUserRoleMapper;
import com.example.springbootdemo.model.Project;
import com.example.springbootdemo.model.ProjectFile;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.IProjectFileService;
import com.example.springbootdemo.service.IProjectService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-04-06
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    ProcessEngineFactoryBean processEngine;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private IProjectFileService projectFileService;




    @Override
    public IPage<Project> getPage(int pageNum, int pageSize,String val) {
        LambdaQueryWrapper<Project> projectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String currentUserId = JwtUtil.getCurrentUserId();
        projectLambdaQueryWrapper.eq(Project::getUserId,currentUserId);
        if(!"1".equals(currentUserId)){
            projectLambdaQueryWrapper.eq(Project::getUserId,currentUserId);
        }

        projectLambdaQueryWrapper.orderByDesc(Project::getCreateTime);
        IPage<Project> page=new Page<>(pageNum,pageSize);
        baseMapper.selectPage(page, projectLambdaQueryWrapper);
        page.getRecords().stream().forEach(project -> {
            SysUser sysUser = sysUserService.getBaseMapper().selectById(project.getUserId());
            project.setUserName(sysUser.getRealName());
            if(StringUtils.isNotBlank(project.getApprovalUserId())){
                String names = sysUserService.selectNameBatchIds(project.getApprovalUserId().split(","));
                project.setApprovalUserName(names);
            }
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(project.getProcessDeptId()).singleResult();
            if(processDefinition!=null){
                project.setProcessDeptName(processDefinition.getName());
                project.setSuspended(processDefinition.isSuspended());
            }
        });
        return page;

    }

    @Override
    public boolean add(Project project) {
        int insert = baseMapper.insert(project);
        project.getProjectFileList().stream().forEach(file->{
            ProjectFile projectFile=new ProjectFile();
            projectFile.setFileUrl(file.getByteUrl());
            projectFile.setProjectId(project.getId());
            projectFile.setFileName(file.getName());
            projectFileService.save(projectFile);
        });
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean edit(Project project) {
        int insert = baseMapper.updateById(project);
        LambdaQueryWrapper<ProjectFile> projectFileLambdaQueryWrapper=new LambdaQueryWrapper<>();
        projectFileLambdaQueryWrapper.eq(ProjectFile::getProjectId,project.getId());
        projectFileService.remove(projectFileLambdaQueryWrapper);
        project.getProjectFileList().stream().forEach(file->{
            ProjectFile projectFile=new ProjectFile();
            projectFile.setFileUrl(file.getByteUrl());
            projectFile.setProjectId(project.getId());
            projectFile.setFileName(file.getName());
            projectFileService.save(projectFile);

        });
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean del(String id) {
        int i = baseMapper.deleteById(id);
        LambdaQueryWrapper<ProjectFile> projectFileLambdaQueryWrapper=new LambdaQueryWrapper<>();
        projectFileLambdaQueryWrapper.eq(ProjectFile::getProjectId,id);
        projectFileService.remove(projectFileLambdaQueryWrapper);
        if(i==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public Project one(String id) {

        Project project = baseMapper.selectById(id);
        SysUser sysUser = sysUserService.getBaseMapper().selectById(project.getUserId());
        project.setUserName(sysUser.getRealName());
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(project.getProcessDeptId()).singleResult();
        if(processDefinition!=null){
            project.setProcessDeptName(processDefinition.getName());
        }
        LambdaQueryWrapper<ProjectFile> projectFileLambdaQueryWrapper=new LambdaQueryWrapper<>();
        projectFileLambdaQueryWrapper.eq(ProjectFile::getProjectId,project.getId());
        List<ProjectFile> list = projectFileService.list(projectFileLambdaQueryWrapper);
        project.setProjectFileList(list);
        return project;
    }

    @Override
    @Transactional
    public boolean startActivity(Project project) {
        Map<String,Object> map=new HashMap<>();
        LambdaUpdateWrapper<Project> projectLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason",project.getReason());
        jsonObject.put("examineStatus","");
        if(!"3".equals(project.getProcessStatus())){
            map.put("projectId",project.getId());
            map.put("day",project.getDay());
            List<String> signList = new ArrayList<>();
            List<String> stringList = Arrays.asList(project.getCountersignApprovals());
            signList.add("fe350d87afde04ae9b20cde87f0b6cd5");
            signList.add("ffc7d22ec17b1f2903ee11870ef40dc9");
            signList.add("0f886b3b0b662248421546a7ab0f7132");
            map.put("datas",stringList);
            map.put("data",JwtUtil.getCurrentUserId());

            ProcessInstance processInstance = runtimeService.startProcessInstanceById(project.getProcessDeptId(), map);
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
            taskService.addComment(task.getId(),task.getProcessInstanceId(),"请假管理提交审批",jsonObject.toJSONString());
            taskService.complete(task.getId());
            projectLambdaUpdateWrapper.eq(Project::getId,project.getId());
            projectLambdaUpdateWrapper.set(Project::getProcessInstanceId,processInstance.getProcessInstanceId());
            List<Task> list = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            List<String> collect = list.stream().filter(task1 -> StringUtils.isNotBlank(task1.getAssignee())).map(Task::getAssignee).collect(Collectors.toList());
            projectLambdaUpdateWrapper.set(Project::getApprovalUserId,String.join(",", collect));
            projectLambdaUpdateWrapper.set(Project::getProcessStatus,"1");
            baseMapper.update(new Project(), projectLambdaUpdateWrapper);
        }else{

            Task task = taskService.createTaskQuery().processInstanceId(project.getProcessInstanceId()).singleResult();
            taskService.addComment(task.getId(),task.getProcessInstanceId(),"请假管理提交审批",jsonObject.toJSONString());
            taskService.complete(task.getId());
            projectLambdaUpdateWrapper.eq(Project::getId,project.getId());
            projectLambdaUpdateWrapper.set(Project::getProcessStatus,"1");

            List<Task> list = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            List<String> collect = list.stream().filter(task1 -> StringUtils.isNotBlank(task1.getAssignee())).map(Task::getAssignee).collect(Collectors.toList());
            projectLambdaUpdateWrapper.set(Project::getApprovalUserId,String.join(",", collect));
            baseMapper.update(new Project(), projectLambdaUpdateWrapper);
        }
        return true;
    }

    @Override
    public IPage<Project> getTaskPage(int pageNum, int pageSize,String approvalStatus) {
        String currentUserId = JwtUtil.getCurrentUserId();
        List<Project> projectArrayList=new ArrayList<>();
        if("1".equals(approvalStatus)){
            List<Task> taskList = taskService.createTaskQuery().taskAssignee(currentUserId).orderByTaskCreateTime().desc().list();


            List<Task> taskCandidateUserList = taskService.createTaskQuery().taskCandidateUser(currentUserId).orderByTaskCreateTime().desc().list();

            taskList.addAll(taskCandidateUserList);

            taskList.stream().forEach(task -> {
                LambdaQueryWrapper<Project> projectLambdaQueryWrapper=new LambdaQueryWrapper<>();
                projectLambdaQueryWrapper.eq(Project::getProcessInstanceId,task.getProcessInstanceId());
                Project project = baseMapper.selectOne(projectLambdaQueryWrapper);
                if(project!=null){
                    project.setTaskId(task.getId());
                    SysUser sysUser = sysUserService.getBaseMapper().selectById(project.getUserId());
                    project.setUserName(sysUser.getRealName());
                    if(StringUtils.isNotBlank(task.getAssignee())){
                        project.setApprovalUserId(task.getAssignee());
                        project.setApprovalUserName(sysUserService.getBaseMapper().selectById(task.getAssignee()).getRealName());
                    }
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(project.getProcessDeptId()).singleResult();
                    if(processDefinition!=null){
                        project.setProcessDeptName(processDefinition.getName());
                        project.setSuspended(processDefinition.isSuspended());
                        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
                        UserTask userTask = (UserTask)bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                        if(userTask.getCandidateGroups().size()>0){
                            project.setType("3");
                        }else if(userTask.getCandidateUsers().size()>0){
                            project.setType("2");
                        }else{
                            project.setType("1");
                        }
                    }
                    projectArrayList.add(project);
                }
            });
        }else{
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssignee(currentUserId).finished().orderByTaskCreateTime().desc().list();
            Set<String> processInstanceIds = list.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
            processInstanceIds.stream().forEach(processInstanceId -> {
                LambdaQueryWrapper<Project> projectLambdaQueryWrapper=new LambdaQueryWrapper<>();
                projectLambdaQueryWrapper.eq(Project::getProcessInstanceId,processInstanceId);
                Project project = baseMapper.selectOne(projectLambdaQueryWrapper);
                if(project!=null){
                    SysUser sysUser = sysUserService.getBaseMapper().selectById(project.getUserId());
                    project.setUserName(sysUser.getRealName());
                    if(StringUtils.isNotBlank(project.getApprovalUserId())){
                        project.setApprovalUserName(sysUserService.getBaseMapper().selectById(project.getApprovalUserId()).getRealName());
                    }
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(project.getProcessDeptId()).singleResult();
                    if(processDefinition!=null){
                        project.setProcessDeptName(processDefinition.getName());
                        project.setSuspended(processDefinition.isSuspended());
                    }
                    projectArrayList.add(project);
                }
            });
        }

        int startResult = (pageNum - 1) * pageSize;
        List<Project> leaveList=projectArrayList.subList(startResult,Math.min(startResult + pageSize, projectArrayList.size()));
        IPage<Project> page=new Page<>();
        page.setTotal(projectArrayList.size());
        page.setRecords(leaveList);
        return page;
    }

    @Override
    @Transactional
    public void complete(Project project) {
        Map<String,Object> map=new HashMap<>();
        Project project1 = baseMapper.selectById(project.getId());
        Task task = taskService.createTaskQuery().taskId(project.getTaskId()).singleResult();
        if("2".equals(project.getProcessStatus())){
            map.put("day",project1.getDay());
            map.put("pass",true);
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",project.getReason());
            jsonObject.put("examineStatus",project.getProcessStatus());
            Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();

            // 获取 nrOfInstances 和 nrOfCompletedInstances
            Integer nrOfInstances = (Integer) runtimeService.getVariable(execution.getId(), "nrOfInstances");
            Integer nrOfCompletedInstances = (Integer) runtimeService.getVariable(execution.getId(), "nrOfCompletedInstances");
            if(nrOfInstances!=null){
                map.put("nrOfCompletedInstances",nrOfCompletedInstances);
                map.put("nrOfInstances",nrOfInstances);
            }
            taskService.addComment(task.getId(),project1.getProcessInstanceId(),"項目管理审批",jsonObject.toJSONString());
            taskService.complete(task.getId(),map);

            List<Task> runTask = taskService.createTaskQuery().processInstanceId(project1.getProcessInstanceId()).list();
            if(runTask.size()==0){
                LambdaUpdateWrapper<Project> projectLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                projectLambdaUpdateWrapper.eq(Project::getId,project.getId());
                projectLambdaUpdateWrapper.set(Project::getProcessStatus,"2");
                projectLambdaUpdateWrapper.set(Project::getApprovalUserId,null);
                baseMapper.update(new Project(),projectLambdaUpdateWrapper);
            }else{
                LambdaUpdateWrapper<Project> projectLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                projectLambdaUpdateWrapper.eq(Project::getId,project.getId());
                List<Task> list = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
                List<String> collect = list.stream().filter(task1 -> StringUtils.isNotBlank(task1.getAssignee())).map(Task::getAssignee).collect(Collectors.toList());
                projectLambdaUpdateWrapper.set(Project::getApprovalUserId,String.join(",", collect));
                baseMapper.update(new Project(),projectLambdaUpdateWrapper);
            }
        }else{
            //驳回
            map.put("pass",false);
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",project.getReason());
            jsonObject.put("examineStatus",project.getProcessStatus());

            taskService.addComment(task.getId(),project1.getProcessInstanceId(),"項目管理审批",jsonObject.toJSONString());
            taskService.complete(task.getId(),map);
            LambdaUpdateWrapper<Project> projectLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            projectLambdaUpdateWrapper.eq(Project::getId,project.getId());
            projectLambdaUpdateWrapper.set(Project::getProcessStatus,"3");
            projectLambdaUpdateWrapper.set(Project::getApprovalUserId,null);
            baseMapper.update(new Project(),projectLambdaUpdateWrapper);

        }
    }
    @Override
    public IPage<Map> historyApproval(Integer pageNum,Integer pageSize,String processInstanceId) {
        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        int startResult = (pageNum - 1) * pageSize;
        List<Comment> pagedComments=processInstanceComments.subList(startResult,Math.min(startResult + pageSize, processInstanceComments.size()));
        List<Map> list=new ArrayList<>();
        for (Comment comment:pagedComments){
            String s = JSON.toJSONString(comment);
            Map map = JSON.parseObject(s, Map.class);
            Map map1=JSON.parseObject(map.get("message").toString(),Map.class);
            map.put("reason",map1.get("reason"));
            map.put("examineStatus",map1.get("examineStatus"));
            if("6".equals(map1.get("examineStatus"))){
                SysUser selectById = sysUserService.getBaseMapper().selectById(map1.get("approvalUserId").toString());

                map.put("approvalUserName",selectById.getRealName());
            }
            SysUser sysUser = sysUserService.getBaseMapper().selectById(comment.getUserId());
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(comment.getTaskId()).singleResult();
            if(sysUser==null){
                map.put("name","");
            }else{
                map.put("name",sysUser.getRealName());
            }
            map.put("activitiName",historicTaskInstance.getName());
            map.put("time", DateFormatUtils.format(comment.getTime(),"yyy-MM-dd HH:mm:ss"));
            list.add(map);
        }
        IPage<Map> page=new Page<>();
        page.setTotal(processInstanceComments.size());
        page.setRecords(list);
        return page;
    }

    @Override
    public void receiveTask(String projectId,String taskId) {
        String userId=JwtUtil.getCurrentUserId();
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason","");
        jsonObject.put("examineStatus","4");
        Project project = baseMapper.selectById(projectId);
        taskService.addComment(taskId,project.getProcessInstanceId(),"项目管理领取",jsonObject.toJSONString());
        taskService.claim(taskId,userId);
        LambdaUpdateWrapper<Project> projectLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        projectLambdaUpdateWrapper.eq(Project::getId,projectId);
        projectLambdaUpdateWrapper.set(Project::getApprovalUserId,userId);
        baseMapper.update(new Project(),projectLambdaUpdateWrapper);
    }

    @Override
    public void returnTask(String projectId,String taskId) {
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason","");
        jsonObject.put("examineStatus","5");
        Project project = baseMapper.selectById(projectId);
        taskService.addComment(taskId,project.getProcessInstanceId(),"项目管理退回",jsonObject.toJSONString());
        taskService.unclaim(taskId);
        LambdaUpdateWrapper<Project> projectLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        projectLambdaUpdateWrapper.eq(Project::getId,projectId);
        projectLambdaUpdateWrapper.set(Project::getApprovalUserId,null);
        baseMapper.update(new Project(),projectLambdaUpdateWrapper);
    }


    /**
     * 流程图跟踪
     * @return
     */
    @Override
    public void getProcessView(String processInstanceId, HttpServletResponse response) throws Exception {
        //获取历史流程实例
        HistoricProcessInstance processInstance =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        processEngineConfiguration = processEngine.getProcessEngineConfiguration();
//        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();

        InputStream imageStream;

        //当前审批流程
        try{
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds,new ArrayList<>(),"宋体","宋体",null,null,1.0);

        }catch (Exception e){
            imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", new ArrayList<>(),new ArrayList<>(),"宋体","宋体",null,null,1.0);

        }

        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }


}

