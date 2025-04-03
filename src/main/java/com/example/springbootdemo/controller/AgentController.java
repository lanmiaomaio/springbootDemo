package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.ButtonPermission;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.Leave;
import com.example.springbootdemo.model.Project;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.ILeaveService;
import com.example.springbootdemo.service.IProjectService;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = "*")
public class AgentController {


    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDictionaryService dictionaryService;

    @Autowired
    private TaskService taskService;


    /**
     * 代办数量
     * @return
     */
    @GetMapping("/agentCount")
    public ResponseBo agentCount() {
        String currentUserId = JwtUtil.getCurrentUserId();
        long taskAssigneeCount = taskService.createTaskQuery().taskAssignee(currentUserId).count();
        long taskCandidateUserCount = taskService.createTaskQuery().taskCandidateUser(currentUserId).count();
        long count = taskAssigneeCount + taskCandidateUserCount;
        return ResponseBo.ok(count);
    }

    /**
     * 请假代办详情
     * @param id
     * @return
     */
    @GetMapping("/leaveOne")
    public ResponseBo leaveOne(String id) {
        if(StringUtils.isNotBlank(id)){
            Map map=new HashMap();
            Leave leave = leaveService.getById(id);
            SysUser sysUser = sysUserService.getById(leave.getUserId());
            leave.setUserName(sysUser.getRealName());
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(leave.getProcessDeptId()).singleResult();
            if(processDefinition!=null){
                leave.setProcessDeptName(processDefinition.getName());
            }
            if(StringUtils.isNotBlank(leave.getLeaveCategory())){
                SysDictionary dictionary = dictionaryService.getById(leave.getLeaveCategory());
                leave.setLeaveCategoryName(dictionary.getName());
            }
            map.put("leave",leave);
            return ResponseBo.ok(map);
        }else{
            return ResponseBo.error("查询失败");
        }
    }

    /**
     * 項目代办详情
     * @param id
     * @return
     */
    @GetMapping("/projectOne")
    public ResponseBo projectOne(String id) {
        if(StringUtils.isNotBlank(id)){
            Map map=new HashMap();
            Project project = projectService.getById(id);
            SysUser sysUser = sysUserService.getById(project.getUserId());
            project.setUserName(sysUser.getRealName());
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(project.getProcessDeptId()).singleResult();
            if(processDefinition!=null){
                project.setProcessDeptName(processDefinition.getName());
            }
            map.put("project",project);
            return ResponseBo.ok(map);
        }else{
            return ResponseBo.error("查询失败");
        }
    }
}
