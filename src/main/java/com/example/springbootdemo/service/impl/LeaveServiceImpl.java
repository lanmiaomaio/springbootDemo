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
import com.example.springbootdemo.service.ILeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
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
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramCanvas;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for (Leave leave:page.getRecords()){
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
        }
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
        leave.setProcessDeptName(processDefinition.getName());
        return leave;
    }

    @Override
//    @Transactional
    public boolean startActivity(Leave leave) {
        String userId=JwtUtil.getCurrentUserId();
        Map<String,Object> map=new HashMap<>();
        String processInstanceId="";
        if(!"3".equals(leave.getProcessStatus())){
            map.put("leaveId",leave.getId());
            map.put("user",userId);
            map.put("houUser",userId);
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(leave.getProcessDeptId(), map);
            processInstanceId=processInstance.getProcessInstanceId();
            Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
            completeTask(leave.getId(),leave.getApprovalUserId(),task.getId());
        }else{
            processInstanceId=leave.getProcessInstanceId();
            Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
            completeTask(leave.getId(),leave.getApprovalUserId(),task.getId());
        }
        LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
        leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
        leaveLambdaUpdateWrapper.set(Leave::getProcessInstanceId,processInstanceId);
        leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"1");
        leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,leave.getApprovalUserId());
        baseMapper.update(new Leave(), leaveLambdaUpdateWrapper);
        return true;
    }

    public void completeTask(String leaveId,String approvalUserId,String taskId){
        Map<String,Object> map=new HashMap<>();
        map.put("leaveId",leaveId);
        map.put("user",approvalUserId);
        map.put("houUser",approvalUserId);
        taskService.complete(taskId,map);
    }

    @Override
    public IPage<Leave> getTaskPage(int pageNum, int pageSize) {
        String currentUserId = JwtUtil.getCurrentUserId();
        List<Leave> leaveList1=new ArrayList<>();
        long count = taskService.createTaskQuery().taskAssignee(currentUserId).count();
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(currentUserId).orderByTaskCreateTime().desc().listPage((pageNum-1)*pageSize,pageSize);
        for (Task task:taskList){
            LambdaQueryWrapper<Leave> leaveLambdaQueryWrapper=new LambdaQueryWrapper<>();
            leaveLambdaQueryWrapper.eq(Leave::getProcessInstanceId,task.getProcessInstanceId());
            Leave leave = baseMapper.selectOne(leaveLambdaQueryWrapper);
            SysUser sysUser = sysUserService.getBaseMapper().selectById(leave.getUserId());
            leave.setUserName(sysUser.getRealName());
            leave.setApprovalUserName(sysUserService.getBaseMapper().selectById(task.getAssignee()).getRealName());
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
            if(processDefinition!=null){
                leave.setProcessDeptName(processDefinition.getName());
            }
            leaveList1.add(leave);
        }
        IPage<Leave> page=new Page<>();
        page.setTotal(count);
        page.setRecords(leaveList1);
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
            map.put("user",leave.getApprovalUserId());
            map.put("houUser",leave.getApprovalUserId());
            Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("reason",leave.getReason());
            jsonObject.put("examineStatus",leave.getProcessStatus());
            taskService.addComment(task.getId(),leave.getProcessInstanceId(),"请假管理审批",jsonObject.toJSONString());
            taskService.complete(task.getId(),map);
        }else{
            //驳回
            map.put("isAgree",false);
            map.put("day",null);
            map.put("user",leave1.getUserId());
            map.put("houUser",leave1.getUserId());

            Authentication.setAuthenticatedUserId(leave1.getUserId());

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
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(leave.getProcessInstanceId()).singleResult();
        if(processInstance==null){
            LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
//            leaveLambdaUpdateWrapper.set(Leave::getProcessInstanceId,leave.getProcessInstanceId());
            leaveLambdaUpdateWrapper.set(Leave::getProcessStatus,"2");
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,null);
            baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
        }else{
            LambdaUpdateWrapper<Leave> leaveLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            leaveLambdaUpdateWrapper.eq(Leave::getId,leave.getId());
//            leaveLambdaUpdateWrapper.set(Leave::getProcessInstanceId,leave.getProcessInstanceId());
            leaveLambdaUpdateWrapper.set(Leave::getApprovalUserId,leave.getApprovalUserId());
            baseMapper.update(new Leave(),leaveLambdaUpdateWrapper);
        }
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
//        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

//        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();

        //高亮环节id集合
//        List<String> highLightedActivitis = new ArrayList<String>();

        //高亮线路id集合
//        List<String> highLightedFlows = getHighLightedFlows(definitionEntity,highLightedActivitList);


//        for(HistoricActivityInstance tempActivity : highLightedActivitList){
//            String activityId = tempActivity.getActivityId();
//            highLightedActivitis.add(activityId);
//        }

        InputStream imageStream;

        //当前审批流程
        try{
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds,new ArrayList<>(),"宋体","宋体",null,null,1.0);

        }catch (Exception e){
            imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", new ArrayList<>(),new ArrayList<>(),"宋体","宋体",null,null,1.0);

        }
        //单独返回流程图，不高亮显示
//        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    @Override
    public List<Map> historyApproval(String processInstanceId) {
        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        List<Map> list=new ArrayList<>();
        for (Comment comment:processInstanceComments){
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
        return list;
    }


    /**
     * 获取已经流转的线
     *
     * @param
     * @param historicActivityInstances
     * @return
     */
    private static List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());// 得到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {
                    // 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;

    }
}

