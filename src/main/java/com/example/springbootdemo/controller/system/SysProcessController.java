package com.example.springbootdemo.controller.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.aspect.ButtonPermission;
import com.example.springbootdemo.common.aspect.Log;
import com.example.springbootdemo.model.system.ProcessBo;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
@RequestMapping("/system/process")
public class SysProcessController {


    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ISysDictionaryService sysDictionaryService;

    @Autowired
    private RuntimeService runtimeService;
    


    /**
     * 查询流程列表
     * @return
     */
    @RequestMapping("/list")
    public ResponseBo list(int pageNum,int pageSize){
        long total=repositoryService.createModelQuery().latestVersion().count();
        List<Model> modelList = repositoryService.createModelQuery().latestVersion().orderByCreateTime().desc().listPage((pageNum - 1) * pageSize, pageSize);
        IPage<ProcessBo> page=new Page<>();
        List<ProcessBo> processDefinitionEntities=new ArrayList<>();
        for (Model model:modelList){
            ProcessBo processBo=new ProcessBo();
            BeanUtils.copyProperties(model,processBo);
            SysDictionary sysDictionary = sysDictionaryService.getById(processBo.getCategory());
            processBo.setCategoryName(sysDictionary.getName());
            if(StringUtils.isNotBlank(model.getMetaInfo())){
                JSONObject jsonObject = JSON.parseObject(model.getMetaInfo());
                processBo.setDescription(jsonObject.getString("description"));
            }
            if(StringUtils.isNotBlank(model.getDeploymentId())){
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(model.getDeploymentId()).singleResult();
                processBo.setSuspensionState(!processDefinition.isSuspended()?1:2);
            }
            processBo.setCreateTime(DateFormatUtils.format(model.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
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
    @ButtonPermission(perm = "sys:process:view")
    public ResponseBo one(String modelId) throws IOException {
        byte[] modelEditorSource = repositoryService.getModelEditorSource(modelId);
        return ResponseBo.ok(0,"查询成功",new String(modelEditorSource));
    }

    /**
     * 添加流程
     * @param xmlString
     * @return+
     */
    @PostMapping("saveModel")
    @ButtonPermission(perm = "sys:process:add")
    @Log(title = "添加流程")
    public ResponseBo saveModel(@RequestBody Map<String, Object> xmlString) {

        Model model = repositoryService.newModel();
        model.setName(xmlString.get("processName").toString());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("description",xmlString.get("description")==null?null:xmlString.get("description").toString());
        model.setMetaInfo(jsonObject.toJSONString());
        model.setCategory(xmlString.get("category").toString());
        model.setKey("process"+ UUID.randomUUID().toString().replace("-",""));
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(),xmlString.get("xmlString").toString().getBytes());
        repositoryService.addModelEditorSourceExtra(model.getId(),xmlString.get("xmlString").toString().getBytes());
        return ResponseBo.ok();
    }

    /**
     * 修改流程
     * @param xmlString
     * @return
     */
    @PostMapping("editModel")
    @ButtonPermission(perm = "sys:process:edit")
    @Log(title = "修改流程")
    public ResponseBo editModel(@RequestBody Map<String, Object> xmlString) {
        Model model = repositoryService.getModel(xmlString.get("modelId").toString());

        if(StringUtils.isNotBlank(model.getDeploymentId())){
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(model.getDeploymentId()).singleResult();
            if(!processDefinition.isSuspended()){
                Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                        .createDeployment()//创建部署对象
                        .name(xmlString.get("processName").toString())//声明流程的名称
                        .addInputStream("qingjia.bpmn", new ByteArrayInputStream(xmlString.get("xmlString").toString().getBytes()))//加载资源文件，一次只能加载一个文件
                        .addInputStream("qingjia.png", new ByteArrayInputStream(xmlString.get("xmlString").toString().getBytes()))//加载资源文件，一次只能加载一个文件
                        .deploy();//完成部署

                Model newModel = repositoryService.newModel();
                newModel.setName(xmlString.get("processName").toString());
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("description",xmlString.get("description")==null?null:xmlString.get("description").toString());
                newModel.setMetaInfo(jsonObject.toJSONString());
                newModel.setCategory(xmlString.get("category").toString());
                newModel.setKey(model.getKey());
                newModel.setVersion(model.getVersion()+1);
                newModel.setDeploymentId(deployment.getId());
                repositoryService.saveModel(newModel);
                repositoryService.addModelEditorSource(newModel.getId(),xmlString.get("xmlString").toString().getBytes());
                repositoryService.addModelEditorSourceExtra(newModel.getId(),xmlString.get("xmlString").toString().getBytes());
            }
        }else{
            Model newModel = repositoryService.newModel();
            newModel.setName(xmlString.get("processName").toString());
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("description",xmlString.get("description")==null?null:xmlString.get("description").toString());
            newModel.setMetaInfo(jsonObject.toJSONString());
            newModel.setCategory(xmlString.get("category").toString());
            newModel.setKey(model.getKey());
            newModel.setVersion(model.getVersion()+1);
            repositoryService.saveModel(newModel);
            repositoryService.addModelEditorSource(newModel.getId(),xmlString.get("xmlString").toString().getBytes());
            repositoryService.addModelEditorSourceExtra(newModel.getId(),xmlString.get("xmlString").toString().getBytes());
        }
        return ResponseBo.ok();
    }


    /**
     * 部署创建流程图
     * @param modelId
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("deployProcess")
    @Log(title = "部署流程")
//    @ButtonPermission(perm = "sys:process:deploy")
    public ResponseBo deployProcess(String modelId) {
        Model model = repositoryService.getModel(modelId);
        byte[] modelEditorSource = repositoryService.getModelEditorSource(modelId);
        Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                .createDeployment()//创建部署对象
                .name(model.getName())//声明流程的名称
                .addInputStream("qingjia.bpmn",new ByteArrayInputStream(modelEditorSource))//加载资源文件，一次只能加载一个文件
                .addInputStream("qingjia.png",new ByteArrayInputStream(modelEditorSource))//加载资源文件，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());//1
        System.out.println("部署时间："+deployment.getDeploymentTime());

        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);
        return ResponseBo.ok();
    }


    /**
     * 根据流程id直接结束流程
     * @paramru
     * @return
     */
    @RequestMapping("/del")
    @ButtonPermission(perm = "sys:process:del")
    @Log(title = "删除流程")
    public ResponseBo del(String modelId){
        Model model = repositoryService.createModelQuery().modelId(modelId).singleResult();
        List<Model> modelList = repositoryService.createModelQuery().modelKey(model.getKey()).list();

        if(StringUtils.isNotBlank(model.getDeploymentId())){
            long count = historyService.createHistoricProcessInstanceQuery().deploymentId(model.getDeploymentId()).count();
            if(count==0){
                modelList.stream().forEach(models -> {
                    repositoryService.deleteModel(models.getId());
                    if(StringUtils.isNotBlank(models.getDeploymentId())){
                        repositoryService.deleteDeployment(models.getDeploymentId(),true);
                    }
                });
            }else {
                return ResponseBo.error("当前流程有绑定的流程无法删除");
            }
        }else{
            modelList.stream().forEach(models -> {
                repositoryService.deleteModel(models.getId());
            });
        }
        return ResponseBo.ok();
    }

    /**
     * 挂起流程
     * @param deploymentId
     */
    @RequestMapping("/pause")
    @ButtonPermission(perm = "sys:process:pause")
    @Log(title = "挂起流程")
    public ResponseBo suspend(String deploymentId) {

        // 查询流程定义
        ProcessDefinition singleResult = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .latestVersion()
                .singleResult();
        // 判断是否被挂起  如果被挂起 则让其激活；激活 则进行挂起操作
        boolean suspended = singleResult.isSuspended();
        if (!suspended) {
            /**
             * 激活  需要进行挂起操作
             * 参数一：流程定义id
             * 参数二：是否暂停
             * 参数三：暂停时间
             */
            repositoryService.suspendProcessDefinitionById(singleResult.getId(),
                    true,
                    null);
        }
        return ResponseBo.ok();
    }

    /**
     * 激活流程
     * @param deploymentId
     */
    @RequestMapping("/active")
    @ButtonPermission(perm = "sys:process:active")
    @Log(title = "激活流程")
    public ResponseBo activate(String deploymentId) {


        // 查询流程定义
        ProcessDefinition singleResult = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .latestVersion()
                .singleResult();
        // 判断是否被挂起  如果被挂起 则让其激活；激活 则进行挂起操作
        boolean suspended = singleResult.isSuspended();
        if (suspended) {
            repositoryService.activateProcessDefinitionById(singleResult.getId(),
                    true,
                    null
            );

            //部署流程
            Model model = repositoryService.createModelQuery().deploymentId(deploymentId).singleResult();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(model.getId());
            Deployment deployment = repositoryService//获取流程定义和部署对象相关的Service
                    .createDeployment()//创建部署对象
                    .name(model.getName())//声明流程的名称
                    .addInputStream("qingjia.bpmn",new ByteArrayInputStream(modelEditorSource))//加载资源文件，一次只能加载一个文件
                    .addInputStream("qingjia.png",new ByteArrayInputStream(modelEditorSource))//加载资源文件，一次只能加载一个文件
                    .deploy();//完成部署
            System.out.println("部署ID："+deployment.getId());//1
            System.out.println("部署时间："+deployment.getDeploymentTime());

            model.setDeploymentId(deployment.getId());
            repositoryService.saveModel(model);
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
     * 获取角色列表
     */

    @RequestMapping("/getRoleList")
    public ResponseBo getRoleList(){
        List<Group> list = identityService.createGroupQuery().list();
        return ResponseBo.ok(list);
    }

}
