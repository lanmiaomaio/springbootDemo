package com.example.springbootdemo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.model.system.SysRole;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.model.system.SysUserRole;
import com.example.springbootdemo.service.IUserService;
import com.example.springbootdemo.service.system.ISysRoleService;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import com.example.springbootdemo.service.system.ISysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class SpringBootDemoApplicationTest {

	private static final Random random = new Random();
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private IdentityService identityService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysRoleService sysRoleService;

	@Autowired
	private IUserService userService;

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


	@Test
	public void testActiviti(){
//		LambdaQueryWrapper<SysUser> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
//		userLambdaQueryWrapper.eq(SysUser::getStatus,"1");
//		List<SysUser> userList = sysUserService.list(userLambdaQueryWrapper);
//		userList.stream().forEach(sysUser -> {
//			User newUser = identityService.newUser(sysUser.getId());
//			newUser.setFirstName(sysUser.getUsername());
//			newUser.setLastName(sysUser.getRealName());
//			identityService.saveUser(newUser);
//		});
		List<SysRole> roleList= sysRoleService.list();
		roleList.stream().forEach(sysRole -> {
			Group newGroup = identityService.newGroup(sysRole.getId());
			newGroup.setName(sysRole.getRoleName());
			identityService.saveGroup(newGroup);
			LambdaQueryWrapper<SysUserRole> userRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
			userRoleLambdaQueryWrapper.eq(SysUserRole::getRoleId,sysRole.getId());
			List<SysUserRole> userRoleList= sysUserRoleService.list(userRoleLambdaQueryWrapper);
			userRoleList.stream().forEach(sysUserRole -> {
				identityService.createMembership(sysUserRole.getUserId(),sysRole.getId());
			});
		});
	}

	@Test
	public void addUser2() throws ParseException {
		for (int i=0;i<50;i++){
			User user=new User();
			user.setName(getRandomJianTiZH(3));
			user.setPhone(generatePhoneNumber());
			Random random1=new Random();
			String[] gardeOptions = {"0e92f3bdf1c1c4ea9b218e7d910d3773", "28e84277ec1259438be2cdc82410f566","07fd8fa1af58f097c3ebce84f412591a"}; // 定义两个选项

			String[] classOptions =
					{"872d66c1fdfd5a31e61a3ab8f21b1e51", "062304cf4474acd766ed669d235011cd",
							"e61431911544f1823e95db7ae4f69921","d388f2f736847c081345ee2d571e7d54",
							"1457dab8dfebdf3907e15b60bca2f65b","ee4c7226768f29d734ec1679882f1125"}; // 定义两个选项

			String[] genderOptions = {"1", "2"}; // 定义两个选项

			user.setClasss(classOptions[random1.nextInt(classOptions.length)]);
			user.setGrade(gardeOptions[random1.nextInt(gardeOptions.length)]);
			user.setGender(genderOptions[random1.nextInt(genderOptions.length)]);
			user.setAddress(getAddr());
			user.setAge((int) (Math.random() * 100 + 1));
			int year=(int)(1970+Math.random()*(2025-1970+1));
			int month=(int)(1+Math.random()*(12-1+1));;
			int date=(int)(1+Math.random()*(28-1+1));;;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			user.setBirthday(sdf.parse(year+"-"+month+"-"+date));
			userService.add(user);
		}
	}


	public static String getAddr(){
		String addrs = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富顺信子杰涛昌成康星光天达岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘华立玉瑞景昊骏泽轩睿逸骏诚石然乐弘伟宇啸天磊瀚文博昊哲瀚冠明辉霖朋健柏鸿涛懿轩烨伟昕磊鹏煊彬益弘文昊强峻熙诚峻豪楠睿祺泰琪霖轩佑擎宇诚昊俊驰松德振豪鹤轩睿诚立绍辉天磊浩然文博昊哲瀚冠霖朋健柏鸿涛懿轩烨伟昕磊鹏煊彬益弘文昊强峻熙诚峻豪楠睿祺泰琪轩佑擎宇诚昊俊驰松德振豪鹤轩睿诚立绍辉昌远涛宇诚材瑾瑜文昊俊驰松德振豪鹤轩睿诚立绍辉天磊浩然文博昊哲瀚冠霖朋健柏鸿涛懿轩烨伟昕磊鹏煊彬益弘文昊强峻熙诚峻豪楠睿祺泰琪轩佑擎宇诚昊俊驰松德振豪鹤轩睿诚立绍辉瀚宇诚材瑾瑜";

		Random random = new Random();
		String addr = "";
		for (int i = 0; i < 4; i++) {
			int index = random.nextInt(300) + 1;
			String lu = addrs.substring(index, index+1);
			addr = addr + lu;
		}
		addr = addr + "路";
		int i = random.nextInt(999) + 1;
		addr = addr + i + "号";
		return addr;
	}


	// 随机生成手机号码的方法
	public static String generatePhoneNumber() {
		Random random = new Random(); // 创建Random对象
		StringBuilder phoneNumber = new StringBuilder("1"); // 先定义数字"1"

		// 随机生成9位数字
		for (int i = 0; i < 9; i++) {
			phoneNumber.append(random.nextInt(10)); // 生成0-9之间的随机数字并添加到phoneNumber中
		}

		return phoneNumber.toString(); // 将StringBuilder转换为字符串并返回
	}

	/**
	 * 自动生成中文名字
	 * @param len 名字的长度
	 * @return
	 */
	public static String getRandomJianTiZH(int len) {
		String ret = "";
		for (int i = 0; i < len; i++) {
			String str = null;
			int hightPos, lowPos; // 定义高低位
			Random random = new Random();
			hightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
			lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
			byte[] b = new byte[2];
			b[0] = (new Integer(hightPos).byteValue());
			b[1] = (new Integer(lowPos).byteValue());
			try {
				str = new String(b, "GBK"); // 转成中文
			} catch (UnsupportedEncodingException ex) {
				ex.printStackTrace();
			}
			ret += str;
		}
		return ret;
	}
}
