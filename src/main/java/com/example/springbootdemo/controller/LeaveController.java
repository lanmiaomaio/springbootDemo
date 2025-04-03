package com.example.springbootdemo.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.util.JwtUtil;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.model.system.ProcessBo;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Leave;
import com.example.springbootdemo.service.ILeaveService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    @ButtonPermission(perm = "leave:add")
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
    @ButtonPermission(perm = "leave:edit")
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
    @ButtonPermission(perm = "leave:view")
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
    @ButtonPermission(perm = "leave:del")
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
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionCategory("5431646bd081c3ac567de98e127101d4").active().latestVersion().list();
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
    @ButtonPermission(perm = "leave:subApprove")
    public ResponseBo startActivity(@RequestBody Leave leave){
        leaveService.startActivity(leave);
        return ResponseBo.ok();
    }

    /**
     * 请假管理-审批历史
     */
    @RequestMapping("/historyApproval")
    @ButtonPermission(perm = "leave:historyApproval")
    public ResponseBo leaveHistoryApproval(Integer pageNum,Integer pageSize,String processInstanceId) {
        IPage<Map> comments = leaveService.historyApproval(pageNum,pageSize,processInstanceId);
        return ResponseBo.ok(comments);
    }



    /**
     * 我的任务
     * @return
     */
    @RequestMapping("/getTaskList")
    public ResponseBo getTaskList(int pageNum, int pageSize,String approvalStatus){
        IPage<Leave> page=leaveService.getTaskPage(pageNum, pageSize,approvalStatus);
        return ResponseBo.ok(page);
    }
    /**
     * 审批
     * @param leave
     * @return
     */
    @PostMapping("/complete")
    @Log(title = "审批")
    @ButtonPermission(perm = "leave:approve:approve")
    public ResponseBo complete(@RequestBody Leave leave){
        leaveService.complete(leave);

        return ResponseBo.ok();
    }

    /**
     * 设置审批人
     * @return
     */
    @Log(title = "指派审批人")
    @GetMapping("/setApprover")
    @ButtonPermission(perm = "leave:approve:assign")
    public ResponseBo setApprover(String leaveId,String taskId,String approvalUserId){
        Authentication.setAuthenticatedUserId(JwtUtil.getCurrentUserId());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("reason","");
        jsonObject.put("examineStatus","6");
        jsonObject.put("approvalUserId",approvalUserId);
        Leave leave = leaveService.getById(leaveId);
        taskService.addComment(taskId,leave.getProcessInstanceId(),"请假管理指派审批人",jsonObject.toJSONString());

        taskService.setAssignee(taskId,approvalUserId);

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
     * 我的审批-审批历史
     */
    @RequestMapping("/approvalHistoryApproval")
    @ButtonPermission(perm = "leave:approve:historyApproval")
    public ResponseBo approvalHistoryApproval(Integer pageNum,Integer pageSize,String processInstanceId) {
        IPage<Map> comments = leaveService.historyApproval(pageNum,pageSize,processInstanceId);
        return ResponseBo.ok(comments);
    }

    /**
     * 领取任务
     */
    @RequestMapping("/receiveTask")
    @Log(title = "领取任务")
    @ButtonPermission(perm = "leave:approve:receive")
    public ResponseBo receiveTask(String leaveId,String taskId) {
        leaveService.receiveTask(leaveId,taskId);
        return ResponseBo.ok();
    }


    /**
     * 退回任务
     */
    @RequestMapping("/returnTask")
    @Log(title = "退回任务")
    @ButtonPermission(perm = "leave:approve:return")
    public ResponseBo returnTask(String leaveId,String taskId) {
        leaveService.returnTask(leaveId,taskId);
        return ResponseBo.ok();
    }

    /**
     * 获取已审批的历史节点
     * @param processInstanceId
     * @return
     */
    @RequestMapping("/getHistoryProcessNode")
    @Log(title = "已审批的历史节点")
    public ResponseBo getHistoryProcessNode(String processInstanceId) {
        List<HistoricActivityInstance> historyProcessNode = leaveService.getHistoryProcessNode(processInstanceId);
        return ResponseBo.ok(historyProcessNode);
    }
}
