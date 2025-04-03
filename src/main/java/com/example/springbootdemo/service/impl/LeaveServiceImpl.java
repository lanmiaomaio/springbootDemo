package com.example.springbootdemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.mapper.system.SysUserRoleMapper;
import com.example.springbootdemo.model.Leave;
import com.example.springbootdemo.mapper.LeaveMapper;
import com.example.springbootdemo.model.Project;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.model.system.SysUserRole;
import com.example.springbootdemo.service.ILeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import com.example.springbootdemo.service.system.ISysUserService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
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
public class LeaveServiceImpl extends ServiceImpl<LeaveMapper, Leave> implements ILeaveService {

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
    private ISysDictionaryService sysDictionaryService;

    @Override
    public IPage<Leave> getPage(int pageNum, int pageSize,String val) {
        LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String currentUserId = JwtUtil.getCurrentUserId();
        if(!"1".equals(currentUserId)){
            leaveLambdaQueryWrapper.eq(Leave::getUserId,currentUserId);

        }
        leaveLambdaQueryWrapper.orderByDesc(Leave::getCreateTime);
        IPage<Leave> page=new Page<>(pageNum,pageSize);
        baseMapper.selectPage(page, leaveLambdaQueryWrapper);
        page.getRecords().stream().forEach(leave -> {
            if (StringUtils.isNotBlank(leave.getLeaveCategory())) {
                SysDictionary dictionary = sysDictionaryService.getById(leave.getLeaveCategory());
                leave.setLeaveCategoryName(dictionary.getName());
            }
            SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
            leave.setUserName(sysUser.getRealName());
            if(StringUtils.isNotBlank(leave.getApprovalUserId())){
                String names = sysUserService.selectNameBatchIds(leave.getApprovalUserId().split(","));
                leave.setApprovalUserName(names);
            }
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
            if(processDefinition!=null){
                leave.setProcessDeptName(processDefinition.getName());
                leave.setSuspended(processDefinition.isSuspended());
            }
        });
        return page;

    }

    @Override
    public boolean add(Leave leave) {
        int insert = baseMapper.insert(leave);
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean edit(Leave leave) {
        int insert = baseMapper.updateById(leave);
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean del(String id) {
        int i = baseMapper.deleteById(id);
        if(i==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public Leave one(String id) {

        Leave leave = baseMapper.selectById(id);
        SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
        leave.setUserName(sysUser.getRealName());
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
        if(processDefinition!=null){
            leave.setProcessDeptName(processDefinition.getName());
        }

        if(StringUtils.isNotBlank(leave.getLeaveCategory())){
            SysDictionary dictionary = sysDictionaryService.getById(leave.getLeaveCategory());
            leave.setLeaveCategoryName(dictionary.getName());
        }
        return leave;
    }

    @Override
    @Transactional
    public boolean startActivity(Leave leave) {
        Map<String,Object> map=new HashMap<>();
        LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason",leave.getReason());
        jsonObject.put("examineStatus","");
        if(!"3".equals(leave.getProcessStatus())){
            map.put("leaveId",leave.getId());
            map.put("day",leave.getDay());
            List<String> signList = new ArrayList<>();
            signList.add("fe350d87afde04ae9b20cde87f0b6cd5");
            signList.add("ffc7d22ec17b1f2903ee11870ef40dc9");
            signList.add("0f886b3b0b662248421546a7ab0f7132");
            map.put("datas",signList);
            map.put("data","c058d7466af60c76b1021d4525c69d5a");

            ProcessInstance processInstance = runtimeService.startProcessInstanceById(leave.getProcessDeptId(), map);
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
            taskService.addComment(task.getId(),task.getProcessInstanceId(),"请假管理提交审批",jsonObject.toJSONString());
            taskService.complete(task.getId());
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessInstanceId,processInstance.getProcessInstanceId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"1");
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();

            List<String> collect = list.stream().filter(task1 -> StringUtils.isNotBlank(task1.getAssignee())).map(Task::getAssignee).collect(Collectors.toList());
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,String.join(",", collect));

            baseMapper.update(new Leave(), leaveLambdaUpdateWrapper);
        }else{

            Task task = taskService.createTaskQuery().processInstanceId(leave.getProcessInstanceId()).singleResult();
            taskService.addComment(task.getId(),task.getProcessInstanceId(),"请假管理提交审批",jsonObject.toJSONString());
            taskService.complete(task.getId());
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"1");
            List<Task> list = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            List<String> collect = list.stream().filter(task1 -> StringUtils.isNotBlank(task1.getAssignee())).map(Task::getAssignee).collect(Collectors.toList());
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,String.join(",", collect));
            baseMapper.update(new Leave(), leaveLambdaUpdateWrapper);
        }
        return true;
    }

    @Override
    public IPage<Leave> getTaskPage(int pageNum, int pageSize,String approvalStatus) {
        String currentUserId = JwtUtil.getCurrentUserId();
        List<Leave> leaveList1=new ArrayList<>();
        if("1".equals(approvalStatus)){
            List<Task> taskList = taskService.createTaskQuery().taskAssignee(currentUserId).orderByTaskCreateTime().desc().list();

            List<Task> taskCandidateUserList = taskService.createTaskQuery().taskCandidateUser(currentUserId).orderByTaskCreateTime().desc().list();

            taskList.addAll(taskCandidateUserList);

            taskList.stream().forEach(task -> {
                LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper=new LambdaQueryWrapper<>();
                leaveLambdaQueryWrapper.eq(Leave::getProcessInstanceId,task.getProcessInstanceId());
                Leave leave = baseMapper.selectOne(leaveLambdaQueryWrapper);
                if(leave!=null){
                    leave.setTaskId(task.getId());
                    SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
                    leave.setUserName(sysUser.getRealName());
                    if(StringUtils.isNotBlank(task.getAssignee())){
                        leave.setApprovalUserId(task.getAssignee());
                        leave.setApprovalUserName(sysUserService.getBaseMapper().selectById(task.getAssignee()).getRealName());
                    }
                    if(StringUtils.isNotBlank(leave.getLeaveCategory())){
                        SysDictionary dictionary = sysDictionaryService.getById(leave.getLeaveCategory());
                        leave.setLeaveCategoryName(dictionary.getName());
                    }
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
                    if(processDefinition!=null){
                        leave.setProcessDeptName(processDefinition.getName());
                        leave.setSuspended(processDefinition.isSuspended());
                        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
                        UserTask userTask = (UserTask)bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                        if(userTask.getCandidateGroups().size()>0){
                            leave.setType("3");
                        }else if(userTask.getCandidateUsers().size()>0){
                            leave.setType("2");
                        }else{
                            leave.setType("1");
                        }
                    }
                    leaveList1.add(leave);

                }
            });
        }else{
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssignee(currentUserId).finished().orderByTaskCreateTime().desc().list();
            Set<String> processInstanceIds = list.stream().filter(historicTaskInstance -> historicTaskInstance.getEndTime()!=null).map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
            processInstanceIds.stream().forEach(processInstanceId -> {
                LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper=new LambdaQueryWrapper<>();
                leaveLambdaQueryWrapper.eq(Leave::getProcessInstanceId,processInstanceId);
                Leave leave = baseMapper.selectOne(leaveLambdaQueryWrapper);
                if(leave!=null){
                    SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
                    leave.setUserName(sysUser.getRealName());
                    if(StringUtils.isNotBlank(leave.getApprovalUserId())){
                        leave.setApprovalUserName(sysUserService.getBaseMapper().selectById(leave.getApprovalUserId()).getRealName());
                    }
                    if(StringUtils.isNotBlank(leave.getLeaveCategory())){
                        SysDictionary dictionary = sysDictionaryService.getById(leave.getLeaveCategory());
                        leave.setLeaveCategoryName(dictionary.getName());
                    }
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
                    if(processDefinition!=null){
                        leave.setProcessDeptName(processDefinition.getName());
                        leave.setSuspended(processDefinition.isSuspended());
                    }
                    leaveList1.add(leave);
                }
            });
        }

        int startResult = (pageNum - 1) * pageSize;
        List<Leave> leaveList=leaveList1.subList(startResult,Math.min(startResult + pageSize, leaveList1.size()));
        IPage<Leave> page=new Page<>();
        page.setTotal(leaveList1.size());
        page.setRecords(leaveList);
        return page;
    }

    @Override
    public List<HistoricActivityInstance> getHistoryProcessNode(String processInstanceId){
        List<HistoricActivityInstance> historicActivities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        List<HistoricActivityInstance> activityInstances = new ArrayList<>();
        List<String> activityIds=new ArrayList<>();
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for (int i=0;i<historicActivities.size();i++){
            if(historicActivities.get(i).getActivityId().equals(taskList.get(0).getTaskDefinitionKey())){
                break;
            }else if(historicActivities.get(i).getEndTime()!=null){
                if(!activityIds.contains(historicActivities.get(i).getActivityId())){
                    activityIds.add(historicActivities.get(i).getActivityId());
                    activityInstances.add(historicActivities.get(i));
                }

            }
        }
        return activityInstances;
    }

    @Override
    @Transactional
    public void complete(Leave leave) {
        Map<String,Object> map=new HashMap<>();
        Leave leave1 = baseMapper.selectById(leave.getId());
        Task task = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
        if("2".equals(leave.getProcessStatus())){
            map.put("day",leave1.getDay());
            map.put("pass",true);
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",leave.getReason());
            jsonObject.put("examineStatus",leave.getProcessStatus());
            Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();

            // 获取 nrOfInstances 和 nrOfCompletedInstances
            Integer nrOfInstances = (Integer) runtimeService.getVariable(execution.getId(), "nrOfInstances");
            Integer nrOfCompletedInstances = (Integer) runtimeService.getVariable(execution.getId(), "nrOfCompletedInstances");
            if(nrOfInstances!=null){
                map.put("nrOfCompletedInstances",nrOfCompletedInstances);
                map.put("nrOfInstances",nrOfInstances);
            }
            taskService.addComment(task.getId(),leave1.getProcessInstanceId(),"请假管理审批同意",jsonObject.toJSONString());
            taskService.complete(task.getId(),map);

            List<Task> runTask = taskService.createTaskQuery().processInstanceId(leave1.getProcessInstanceId()).list();
            if(runTask.size()==0){
                LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
                leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"2");
                leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,null);

                baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
            }else{
                LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
                List<Task> list = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
                List<String> collect = list.stream().filter(task1 -> StringUtils.isNotBlank(task1.getAssignee())).map(Task::getAssignee).collect(Collectors.toList());
                leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,String.join(",", collect));
                baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
            }
        }else{
            //驳回
            map.put("pass",false);
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",leave.getReason());
            jsonObject.put("examineStatus",leave.getProcessStatus());
            jsonObject.put("activityNode",leave.getTargetNodeId());
            taskService.addComment(task.getId(),leave1.getProcessInstanceId(),"请假管理审批驳回",jsonObject.toJSONString());
            rejectToNode(task.getId(),leave.getTargetNodeId());
            LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"3");
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,null);
            baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);

        }
    }

    /**
     * 驳回
     */
    public void rejectToNode(String taskId,String targetNodeId) {
        try {
            Map<String, Object> variables;
            // 取得当前任务.当前任务节点
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(taskId)
                    .singleResult();
            // 取得流程实例，流程实例
            ProcessInstance instance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(currTask.getProcessInstanceId())
                    .singleResult();
            if (instance != null) {
                variables = instance.getProcessVariables();
                // 取得流程定义
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(currTask
                                .getProcessDefinitionId());
                if (definition != null) {
                    //取得当前活动节点
                    ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                            .findActivity(currTask.getTaskDefinitionKey());

                    // 取得上一步活动
                    //也就是节点间的连线
                    //获取来源节点的关系
                    List<PvmTransition> nextTransitionList = currActivity.getIncomingTransitions();

                    // 清除当前活动的出口
                    List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
                    //新建一个节点连线关系集合
                    //获取出口节点的关系
                    List<PvmTransition> pvmTransitionList = currActivity
                            .getOutgoingTransitions();
                    //
                    for (PvmTransition pvmTransition : pvmTransitionList) {
                        oriPvmTransitionList.add(pvmTransition);
                    }
                    pvmTransitionList.clear();

                    // 建立新出口
                    List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
//                    for (PvmTransition nextTransition : nextTransitionList) {
//                        PvmActivity nextActivity = nextTransition.getSource();

                        //destTaskkey
                        ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition)
                                // .findActivity(nextActivity.getId());
                                .findActivity(targetNodeId);
                        TransitionImpl newTransition = currActivity
                                .createOutgoingTransition();
                        newTransition.setDestination(nextActivityImpl);
                        newTransitions.add(newTransition);
//                    }
                    // 完成任务
                    List<Task> tasks = taskService.createTaskQuery()
                            .processInstanceId(instance.getId())
                            .taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
                    for (Task task : tasks) {
                        taskService.complete(task.getId(), variables);
//                        historyService.deleteHistoricTaskInstance(task.getId());
                    }
                    // 恢复方向
                    for (TransitionImpl transitionImpl : newTransitions) {
                        currActivity.getOutgoingTransitions().remove(transitionImpl);
                    }
                    for (PvmTransition pvmTransition : oriPvmTransitionList) {

                        pvmTransitionList.add(pvmTransition);
                    }

                }
            }

        } catch (Exception e) {

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
            if("3".equals(map1.get("examineStatus"))){
                if(map1.get("activityNode")!=null){
                    List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).activityId(map1.get("activityNode").toString()).list();
                    map.put("activityNode",activityInstances.get(0).getActivityName());
                }else{
                    map.put("activityNode","");
                }
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
    @Transactional
    public void receiveTask(String leaveId,String taskId) {
        String userId=JwtUtil.getCurrentUserId();
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason","");
        jsonObject.put("examineStatus","4");
        Leave leave = baseMapper.selectById(leaveId);
        taskService.addComment(taskId,leave.getProcessInstanceId(),"请假管理领取",jsonObject.toJSONString());

        taskService.claim(taskId,userId);
        LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        leaveLambdaUpdateWrapper.eq(Leave::getId,leaveId);
        leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,userId);
        baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
    }

    @Override
    public void returnTask(String leaveId,String taskId) {
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason","");
        jsonObject.put("examineStatus","5");
        Leave leave = baseMapper.selectById(leaveId);
        taskService.addComment(taskId,leave.getProcessInstanceId(),"请假管理退回",jsonObject.toJSONString());
        taskService.unclaim(taskId);
        LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        leaveLambdaUpdateWrapper.eq(Leave::getId,leaveId);
        leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,null);
        baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
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
            imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds,new ArrayList<>(),"宋体","微软雅黑","黑体",null,1.0);

        }catch (Exception e){
            imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", new ArrayList<>(),new ArrayList<>(),"宋体","微软雅黑","黑体",null,1.0);

        }

        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }


}

