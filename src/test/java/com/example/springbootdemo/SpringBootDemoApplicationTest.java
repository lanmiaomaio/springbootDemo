package com.example.springbootdemo;

import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.service.system.ISysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.*;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class SpringBootDemoApplicationTest {

	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private ISysUserService sysUserService;

	@Test
	public void addUser(){
		SysUser sysUser=new SysUser();
		sysUser.setPassword("123456");
		sysUser.setUsername("admin");
		sysUser.setPassword(MD5Utils.encrypt(sysUser.getUsername(),sysUser.getPassword()));
		boolean save = sysUserService.add(sysUser);
	}
	/**
	 * 启动流程
	 * @return
	 */
	@Test
	public void startActivity(){
		Map<String,Object> map=new HashMap<>();
		map.put("user","1");
		map.put("houUser","1");
		ProcessInstance processInstance = runtimeService.startProcessInstanceById("process-qingjia:14:302504",map);
		System.out.println("流程定义id-" + processInstance.getProcessDefinitionId());
		System.out.println("流程实例id-" + processInstance.getId());
		System.out.println("当前活动Id-" + processInstance.getActivityId());
	}

	@Test
	public void saveModel() throws IOException {
		Model modelData=repositoryService.newModel();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode modelObjectNode = objectMapper.createObjectNode();
		modelObjectNode.put("name", "请假流程");
		modelObjectNode.put("description", "流程描述");// 描述
		modelData.setMetaInfo(modelObjectNode.toString());// 设置MetaInfo属性数据

		modelData.setName("请假流程");
		modelData.setKey("流程key");
		modelData.setCategory("分类");
		repositoryService.saveModel(modelData);
		InputStream inputstream = new ClassPathResource("/newBpmn.bpmn20.xml").getInputStream();
		byte[] byt=IOUtils.toByteArray(inputstream);
		repositoryService.addModelEditorSource(	modelData.getId(),byt);
		InputStream svgStream = new ByteArrayInputStream(byt);
		byte[] result = IOUtils.toByteArray(svgStream);
		repositoryService.addModelEditorSourceExtra(modelData.getId(), result);
		System.out.println("2222");
	}

	/**
	 * 获取待办
	 * @return
	 */
	@Test
	public void getTaskList(){
		int pageNum=1;
		int pageSize=15;
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("zhangsan").listPage((pageNum-1)*pageSize,pageSize);
		System.out.println(taskList);
	}

	/**
	 * 提交
	 * @param
	 * @return
	 */
	@Test
	public void complete(){
		String taskId="337506";
		Map<String,Object> map=new HashMap<>();
		map.put("user","c058d7466af60c76b1021d4525c69d5a");
		map.put("houUser","c058d7466af60c76b1021d4525c69d5a");
		map.put("day",3);
		map.put("isAgree",true);
		taskService.complete(taskId,map);
	}

	/**
	 * 驳回
	 * @param
	 * @return
	 */
	@Test
	public void rejectComplete(){
		String taskId="332504";
		Map<String,Object> map=new HashMap<>();
		map.put("user","c058d7466af60c76b1021d4525c69d5a");
		map.put("houUser","c058d7466af60c76b1021d4525c69d5a");
		map.put("isAgree",false);
		taskService.complete(taskId,map);
	}
	/**
	 * 根据流程id直接结束流程
	 * @paramru
	 * @return
	 */
	@Test
	public void deleteProcessInstance(){
		repositoryService.deleteDeployment("30013",true);
	}


	@Test
	public void testQueryBpmnFile() throws Exception {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId("process:3:10012").singleResult();
		String deploymentId = processDefinition.getDeploymentId();
		InputStream pngInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getDiagramResourceName());
		InputStream bpmnInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
		File filePng = new File("D:/leave.png");
		File fileBpmn = new File("D:/leave.bpmn");
		FileOutputStream pngOutput = new FileOutputStream(filePng);
		FileOutputStream bpmnOutput = new FileOutputStream(fileBpmn);
		IOUtils.copy(pngInput, pngOutput);
		IOUtils.copy(bpmnInput, bpmnOutput);
		bpmnOutput.close();
		pngOutput.close();
		bpmnInput.close();
		pngInput.close();
	}


}
