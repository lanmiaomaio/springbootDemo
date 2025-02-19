package com.example.springbootdemo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.Log;
import com.example.springbootdemo.common.pojo.ProcessBo;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysUserService;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
@RequestMapping("/system/process")
public class SysProcessController {


    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;
    


    /**
     * 查询流程列表
     * @return
     */
    @RequestMapping("/list")
    public ResponseBo list(int pageNum,int pageSize){
        long total=repositoryService.createProcessDefinitionQuery().latestVersion().count();
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().latestVersion().listPage((pageNum - 1) * pageSize, pageSize);
        IPage<ProcessBo> page=new Page<>();
        List<ProcessBo> processDefinitionEntities=new ArrayList<>();
        for (ProcessDefinition processDefinition:processDefinitionList){
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(processDefinition.getDeploymentId()).singleResult();
            ProcessBo processBo=new ProcessBo();
            BeanUtils.copyProperties(processDefinition,processBo);
            processBo.setCreateTime(DateFormatUtils.format(deployment.getDeploymentTime(),"yyyy-MM-dd HH:mm:ss"));
            processDefinitionEntities.add(processBo);
        }
        page.setTotal(total);
        page.setRecords(processDefinitionEntities);
        return ResponseBo.ok(page);
    }

    /**
     * 查询流程
     */
    @RequestMapping("/one")
    public ResponseBo one(String deploymentId) throws IOException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(deploymentId).singleResult();
        String diagramResourceName = processDefinition.getDiagramResourceName();
        InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramResourceName);
        return ResponseBo.ok(0,"查询成功",new String(IOUtils.toByteArray(inputStream)));
    }

    /**
     * 部署创建流程图
     * @param xmlString
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("getProcess")
    @Log(title = "添加流程")
    public ResponseBo getProcess(@RequestBody Map<String, Object> xmlString) {
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .name("请假审批流程")//声明流程的名称
                .addInputStream("qingjia.bpmn",new ByteArrayInputStream(xmlString.get("xmlString").toString().getBytes()))//加载资源文件，一次只能加载一个文件
                .addInputStream("qingjia.png",new ByteArrayInputStream(xmlString.get("xmlString").toString().getBytes()))//加载资源文件，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());//1
        System.out.println("部署时间："+deployment.getDeploymentTime());
        return ResponseBo.ok();
    }

    /**
     * 根据流程id直接结束流程
     * @paramru
     * @return
     */
    @RequestMapping("/del")
    @Log(title = "删除流程")
    public ResponseBo del(String id){
        long count = historyService.createHistoricProcessInstanceQuery().deploymentId(id).count();
        if(count==0){
            repositoryService.deleteDeployment(id,true);
        }else {
            return ResponseBo.error("当前流程有绑定的流程无法删除");
        }
        return ResponseBo.ok();
    }


    /**
     * 获取用户列表
     * @paramru
     * @return
     */
    @RequestMapping("/getUserList")
    public ResponseBo getUserList(){
        List<User> list = identityService.createUserQuery().list();
        return ResponseBo.ok(list);
    }

    /**
     * 货期角色列表
     */

    @RequestMapping("/getRoleList")
    public ResponseBo getRoleList(){
        List<Group> list = identityService.createGroupQuery().list();
        return ResponseBo.ok(list);
    }

}
