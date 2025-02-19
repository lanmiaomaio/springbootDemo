package com.example.springbootdemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.model.Leave;
import com.example.springbootdemo.mapper.LeaveMapper;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.model.system.SysUserRole;
import com.example.springbootdemo.service.ILeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import com.example.springbootdemo.service.system.ISysUserService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
    private ISysDictionaryService dictionaryService;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    ProcessEngineFactoryBean processEngine;

    @Autowired
    private IdentityService identityService;

    @Autowired
    ISysUserRoleService sysUserRoleService;

    @Override
    public IPage<Leave> getPage(int pageNum, int pageSize,String val) {
        LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(val)){
            String currentUserId = JwtUtil.getCurrentUserId();
            leaveLambdaQueryWrapper.eq(Leave::getApprovalUserId,currentUserId).eq(Leave::getProcessStatus,"1");
        }
        leaveLambdaQueryWrapper.orderByDesc(Leave::getCreateTime);
        IPage<Leave> page=new Page<>(pageNum,pageSize);
        baseMapper.selectPage(page, leaveLambdaQueryWrapper);
        page.getRecords().stream().forEach(leave -> {
            SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
            leave.setUserName(sysUser.getRealName());
            if(StringUtils.isNotBlank(leave.getApprovalUserId())){
                SysUser sysUser1 = sysUserService.getBaseMapper().selectById(leave.getApprovalUserId());
                leave.setApprovalUserName(sysUser1.getRealName());
            }
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
            if(processDefinition!=null){
                leave.setProcessDeptName(processDefinition.getName());
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
        return leave;
    }

    @Override
//    @Transactional
    public boolean startActivity(Leave leave) {
        Map<String,Object> map=new HashMap<>();
        LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        if(!"3".equals(leave.getProcessStatus())){
            map.put("leaveId",leave.getId());
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(leave.getProcessDeptId(), map);
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessInstanceId,processInstance.getProcessInstanceId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"1");
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,task.getAssignee());
            baseMapper.update(new Leave(), leaveLambdaUpdateWrapper);
        }else{
            Task task = taskService.createTaskQuery().processInstanceId(leave.getProcessInstanceId()).singleResult();
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"1");
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,task.getAssignee());
            baseMapper.update(new Leave(), leaveLambdaUpdateWrapper);
        }
        return true;
    }

    @Override
    public IPage<Leave> getTaskPage(int pageNum, int pageSize) {
        String currentUserId = JwtUtil.getCurrentUserId();
        List<Leave> leaveList1=new ArrayList<>();
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(currentUserId).orderByTaskCreateTime().desc().list();

        taskList.stream().forEach(task -> {task.setCategory("assignee");});
        List<Task> taskCandidateUserList = taskService.createTaskQuery().taskCandidateUser(currentUserId).orderByTaskCreateTime().desc().list();
        taskCandidateUserList.stream().forEach(task -> {task.setCategory("candidateUserGroup");});

//        LambdaQueryWrapper<SysUserRole> userRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
//        userRoleLambdaQueryWrapper.eq(SysUserRole::getUserId,currentUserId);
//        List<SysUserRole> list = sysUserRoleService.list(userRoleLambdaQueryWrapper);
//        List<String> ids = list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
//        List<Task> taskCandidateGroupList = taskService.createTaskQuery().taskCandidateGroupIn(ids).orderByTaskCreateTime().desc().list();
//        taskCandidateGroupList.stream().forEach(task -> {task.setCategory("candidateGroup");});
        taskList.addAll(taskCandidateUserList);
//        taskList.addAll(taskCandidateGroupList);
        taskList.stream().forEach(task -> {
            LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper=new LambdaQueryWrapper<>();
            leaveLambdaQueryWrapper.eq(Leave::getProcessInstanceId,task.getProcessInstanceId());
            Leave leave = baseMapper.selectOne(leaveLambdaQueryWrapper);
            if(leave!=null){
                if(!"3".equals(leave.getProcessStatus())){
                    leave.setTaskId(task.getId());
                    SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
                    leave.setUserName(sysUser.getRealName());
                    if(StringUtils.isNotBlank(task.getAssignee())){
                        leave.setApprovalUserName(sysUserService.getBaseMapper().selectById(task.getAssignee()).getRealName());
                    }
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
                    if(processDefinition!=null){
                        leave.setProcessDeptName(processDefinition.getName());
                    }
                    leave.setType(task.getCategory());
                    leaveList1.add(leave);
                }
            }
        });
        int startResult = (pageNum - 1) * pageSize;
        List<Leave> leaveList=leaveList1.subList(startResult,Math.min(startResult + pageSize, leaveList1.size()));
        IPage<Leave> page=new Page<>();
        page.setTotal(leaveList1.size());
        page.setRecords(leaveList);
        return page;
    }

    @Override
    @Transactional
    public void complete(Leave leave) {
        Map<String,Object> map=new HashMap<>();
        Leave leave1 = baseMapper.selectById(leave.getId());
        Task task = taskService.createTaskQuery().processInstanceId(leave.getProcessInstanceId()).singleResult();
        if("2".equals(leave.getProcessStatus())){
            map.put("day",leave1.getDay());
            map.put("isAgree",true);
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",leave.getReason());
            jsonObject.put("examineStatus",leave.getProcessStatus());
            taskService.addComment(task.getId(),leave.getProcessInstanceId(),"请假管理审批",jsonObject.toJSONString());
            taskService.complete(task.getId(),map);

            Task runTask = taskService.createTaskQuery().processInstanceId(leave.getProcessInstanceId()).singleResult();
            if(runTask==null){
                LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
                leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"2");
                leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,null);
                baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
            }else{
                LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
                leaveLambdaUpdateWrapper.set(Leave::getProcessInstanceId,runTask.getProcessInstanceId());
                leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,runTask.getAssignee());
                baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
            }
        }else{
            //驳回
            map.put("isAgree",false);
            map.put("day",null);
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",leave.getReason());
            jsonObject.put("examineStatus",leave.getProcessStatus());
            taskService.addComment(task.getId(),leave.getProcessInstanceId(),"请假管理审批",jsonObject.toJSONString());
            taskService.complete(task.getId(),map);
            LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"3");
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,null);
            baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
//            taskService.deleteTask(leave.getTaskId(),false);

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
            SysUser sysUser = sysUserService.getBaseMapper().selectById(comment.getUserId());
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(comment.getTaskId()).singleResult();
            map.put("name",sysUser.getRealName());
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
    public void receiveTask(String leaveId,String taskId) {
        String userId=JwtUtil.getCurrentUserId();
        taskService.claim(taskId,userId);
        LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        leaveLambdaUpdateWrapper.eq(Leave::getId,leaveId);
        leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,userId);
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
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

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

