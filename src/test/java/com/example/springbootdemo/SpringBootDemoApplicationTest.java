package com.example.springbootdemo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.springbootdemo.common.JwtUtil;
import com.example.springbootdemo.common.util.MD5Utils;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.model.Course;
import com.example.springbootdemo.model.ScoreUser;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.model.system.SysDictionary;
import com.example.springbootdemo.model.system.SysRole;
import com.example.springbootdemo.model.system.SysUser;
import com.example.springbootdemo.model.system.SysUserRole;
import com.example.springbootdemo.service.ICourseService;
import com.example.springbootdemo.service.IScoreUserService;
import com.example.springbootdemo.service.IUserService;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysRoleService;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import com.example.springbootdemo.service.system.ISysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.springframework.amqp.rabbit.core.RabbitAdmin.QUEUE_NAME;

@SpringBootTest
class SpringBootDemoApplicationTest {


	private static final String[] NAME_LIST = {
			"赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨",
			"朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜",
			"戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎",
			"鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐",
			"费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安", "常",
			"乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余", "元", "卜", "顾", "孟", "平", "黄",
			"和", "穆", "萧", "尹", "姚", "邵", "湛", "汪", "祁", "毛", "禹", "狄", "米", "贝", "明", "臧",
			"计", "伏", "成", "戴", "谈", "宋", "茅", "庞", "熊", "纪", "舒", "屈", "项", "祝", "董", "梁",
			"杜", "阮", "蓝", "闵", "席", "季", "麻", "强", "贾", "路", "娄", "危", "江", "童", "颜", "郭",
			"梅", "盛", "林", "刁", "钟", "徐", "邱", "骆", "高", "夏", "蔡", "田", "樊", "胡", "凌", "霍",
			"虞", "万", "支", "柯", "昝", "管", "卢", "莫", "经", "房", "裘", "缪", "干", "解", "应", "宗",
			"丁", "宣", "贲", "邓", "郁", "单", "杭", "洪", "包", "诸", "左", "石", "崔", "吉", "钮", "龚",
			"程", "嵇", "邢", "滑", "裴", "陆", "荣", "翁", "荀", "羊", "於", "惠", "甄", "曲", "家", "封",
			"芮", "羿", "储", "靳", "汲", "邴", "糜", "松", "井", "段", "富", "巫", "乌", "焦", "巴", "弓",
			"牧", "隗", "山", "谷", "车", "侯", "宓", "蓬", "全", "郗", "班", "仰", "秋", "仲", "伊", "宫",
			"宁", "仇", "栾", "暴", "甘", "钭", "厉", "戎", "祖", "武", "符", "刘", "景", "詹", "束", "龙",
			"叶", "幸", "司", "韶", "郜", "黎", "蓟", "薄", "印", "宿", "白", "怀", "蒲", "邰", "从", "鄂",
			"索", "咸", "籍", "赖", "卓", "蔺", "屠", "蒙", "池", "乔", "阴", "郁", "胥", "能", "苍", "双",
			"闻", "莘", "党", "翟", "谭", "贡", "劳", "逄", "姬", "申", "扶", "堵", "冉", "宰", "郦", "雍",
			"郤", "璩", "桑", "桂", "濮", "牛", "寿", "通", "边", "扈", "燕", "冀", "郏", "浦", "尚", "农",
			"温", "别", "庄", "晏", "柴", "瞿", "阎", "充", "慕", "连", "茹", "习", "宦", "艾", "鱼", "容",
			"向", "古", "易", "慎", "戈", "廖", "庾", "终", "暨", "居", "衡", "步", "都", "耿", "满", "弘",
			"匡", "国", "文", "寇", "广", "禄", "阙", "东", "欧", "殳", "沃", "利", "蔚", "越", "夔", "隆",
			"师", "巩", "厍", "聂", "晁", "勾", "敖", "融", "冷", "訾", "辛", "阚", "那", "简", "饶", "空",
			"曾", "毋", "沙", "乜", "养", "鞠", "须", "丰", "巢", "关", "蒯", "相", "查", "后", "荆", "红",
			"游", "竺", "权", "逯", "盖", "益", "桓", "公", "万俟", "司马", "上官", "欧阳", "夏侯", "诸葛",
			"闻人", "东方", "赫连", "皇甫", "尉迟", "公羊", "澹台", "公冶", "宗政", "濮阳", "淳于", "单于",
			"太叔", "申屠", "公孙", "仲孙", "轩辕", "令狐", "钟离", "宇文", "长孙", "慕容", "鲜于", "闾丘",
			"司徒", "司空", "亓官", "司寇", "仉督", "子车", "颛孙", "端木", "巫马", "公西", "漆雕", "乐正",
			"壤驷", "公良", "拓跋", "夹谷", "宰父", "谷梁", "晋", "楚", "闫", "法", "汝", "鄢", "涂", "钦",
			"段干", "百里", "东郭", "南门", "呼延", "归海", "羊舌", "微生", "岳", "帅", "缑", "亢", "况",
			"郈", "有", "琴", "梁丘", "左丘", "东门", "西门", "商", "牟", "佘", "佴", "伯", "赏", "南宫",
			"墨", "哈", "谯", "笪", "年", "爱", "阳", "佟"
	};

	private static final String[] COMMON_CHARACTERS = {
			"伟", "芳", "强", "丽", "敏", "静", "杰", "慧", "磊", "娜", "超", "玲", "勇", "丹", "军", "艳",
			"涛", "霞", "明", "梅", "峰", "兰", "刚", "萍", "辉", "秀", "波", "桂", "鹏", "菊", "林", "翠",
			"俊", "青", "龙", "荣", "华", "英", "忠", "玉", "凯", "平", "健", "红", "婷", "祥", "瑞", "云",
			"雪", "飞", "亮", "芬", "宇", "文", "浩", "甜", "佳", "博", "思", "萌", "颖", "振", "海", "冰",
			"晨", "露", "泽", "宁", "雨", "嘉", "瑶", "润", "琴", "帆", "琪", "昕", "梦", "阳", "依", "风",
			"清", "影", "悦", "新", "源", "媛", "薇", "可", "靖", "萱", "卿", "韵", "逸", "柏", "艺", "馨",
			"羽", "菱", "君", "碧", "锦", "菱", "熙", "菱", "念", "悠", "宛", "若", "诗", "珊", "菱", "芷",
			"菱", "芙", "榆", "菱", "檀", "菱", "樱", "菱", "玫", "菱", "荷", "菱", "莲", "柳", "梨", "槐",
			"桃", "菱", "竹", "菱", "松", "菱", "柏", "菱", "榕", "菱", "枫", "柯", "桦", "桐", "梓", "榕",
			"杞", "檀", "棉", "棕", "楼",  "椿"};
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

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private ISysDictionaryService dictionaryService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private ICourseService courseService;

	@Autowired
	private IScoreUserService scoreUserService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

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
		String taskId="170056";
		Map<String,Object> map=new HashMap<>();
		map.put("pass",false);
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
	public void addUser1(){
		LambdaQueryWrapper<User> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
		userLambdaQueryWrapper.eq(User::getGrade,"07fd8fa1af58f097c3ebce84f412591a");
		List<User> list = userService.list(userLambdaQueryWrapper);
		for (int i=0;i<list.size();i++){
			LambdaUpdateWrapper<User> userLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
			userLambdaUpdateWrapper.eq(User::getId,list.get(i).getId());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			String format = sdf.format(new Date());
			String userNo=format+"03"+String.format("%04d", i+1);
			userLambdaUpdateWrapper.set(User::getUserNo,userNo);
			userService.update(userLambdaUpdateWrapper);
		}
	}

	@Test
	public void addUser2() throws ParseException {
		List<User> userArrayList=new ArrayList<>();

		for (int i=0;i<88;i++){
			User user=new User();
			String gradeName="高二";
			LambdaQueryWrapper<Course> gradeDictionaryQueryWrapper=new LambdaQueryWrapper<>();
			gradeDictionaryQueryWrapper.eq(Course::getTitle,gradeName);
			List<Course> dictionaryGradeList = courseService.list(gradeDictionaryQueryWrapper);
			if (dictionaryGradeList.size()>0){
				user.setGrade(dictionaryGradeList.get(0).getId());
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			String format = sdf.format(new Date());
			String formatDate="高一".equals(gradeName)?format: "高二".equals(gradeName)?String.valueOf(Integer.parseInt(format)-1): "高三".equals(gradeName)?String.valueOf(Integer.parseInt(format)-2):format;
			String gradeParam="高一".equals(gradeName)?"01": "高二".equals(gradeName)?"02": "高三".equals(gradeName)?"03":"04";
			String maxUserNo = userMapper.selectMaxUserNo(format + gradeParam);
			user.setUserNo(formatDate+gradeParam+String.format("%04d", (StringUtils.isNotBlank(maxUserNo)?Integer.parseInt(maxUserNo):0) +i+1));
			user.setName(getRandomJianTiZH());
			user.setPhone(generatePhoneNumber());
			Random random1=new Random();
//			String[] gardeOptions = {"0e92f3bdf1c1c4ea9b218e7d910d3773", "28e84277ec1259438be2cdc82410f566","07fd8fa1af58f097c3ebce84f412591a"}; // 定义两个选项

			LambdaQueryWrapper<Course> dictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
			dictionaryLambdaQueryWrapper.eq(Course::getParentId,user.getGrade()).eq(Course::getType,"class").orderByAsc(Course::getSortBy);
			List<Course> list= courseService.list(dictionaryLambdaQueryWrapper);
			String[] classOptions = list.stream().map(Course::getId).toArray(String[]::new);
//			String[] classOptions =
//					{"062304cf4474acd766ed669d235011cd", "872d66c1fdfd5a31e61a3ab8f21b1e51",
//							"e61431911544f1823e95db7ae4f69921","ee4c7226768f29d734ec1679882f1125",}; // 定义两个选项

			String[] genderOptions = {"1", "2"}; // 定义两个选项

			user.setClasss(classOptions[random1.nextInt(classOptions.length)]);
			user.setGender(genderOptions[random1.nextInt(genderOptions.length)]);
			user.setAddress(getAddr());
			user.setAge((int) (Math.random() * 100 + 1));
			int year=(int)(1970+Math.random()*(2025-1970+1));
			int month=(int)(1+Math.random()*(12-1+1));;
			int date=(int)(1+Math.random()*(28-1+1));;;
			user.setStatus("1");
			user.setBirthday(sdf.parse(year+"-"+month+"-"+date));
			user.setCreateTime(LocalDateTime.now());
			userArrayList.add(user);
		}

		userArrayList.stream().forEach(user -> {
			userMapper.insert(user);
		});
	}


	@Test
	public void addUser3() throws ParseException {
		List<User> userArrayList=new ArrayList<>();

		List<User> userList = userService.list();
		for (int i=0;i<userList.size();i++){
			User user=new User();
			Random random1=new Random();
			BeanUtils.copyProperties(userList.get(i),user);
			LambdaQueryWrapper<Course> dictionaryLambdaQueryWrapper=new LambdaQueryWrapper<>();
			dictionaryLambdaQueryWrapper.eq(Course::getParentId,userList.get(i).getGrade()).eq(Course::getType,"class").orderByAsc(Course::getSortBy);
			List<Course> list= courseService.list(dictionaryLambdaQueryWrapper);
			String[] classOptions = list.stream().map(Course::getId).toArray(String[]::new);

			user.setClasss(classOptions[random1.nextInt(classOptions.length)]);

			userArrayList.add(user);
		}

		userArrayList.stream().forEach(user -> {
			userMapper.updateById(user);
		});
	}

	@Test
	public void addUser4() throws ParseException {

		List<User> userList = userService.list();
		for (int i=0;i<userList.size();i++){
			LambdaUpdateWrapper<ScoreUser> scoreUserLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
			scoreUserLambdaUpdateWrapper.eq(ScoreUser::getUserId,userList.get(i).getId());
			scoreUserLambdaUpdateWrapper.set(ScoreUser::getClasss,userList.get(i).getClasss());
			scoreUserService.update(scoreUserLambdaUpdateWrapper);
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
	 * @param
	 * @return
	 */
	public static String getRandomJianTiZH() {

		Random ra = new Random();
		return NAME_LIST[ra.nextInt(NAME_LIST.length - 1)].concat(COMMON_CHARACTERS[ra.nextInt(COMMON_CHARACTERS.length - 1)]).concat(COMMON_CHARACTERS[ra.nextInt(COMMON_CHARACTERS.length - 1)]);
	}

	@Test
	public void rejectToNode() {
		String taskId = "335034";
		String targetNodeId = "Activity_1spe10p";
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
					for (PvmTransition nextTransition : nextTransitionList) {
						PvmActivity nextActivity = nextTransition.getSource();

						//destTaskkey
						ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition)
								// .findActivity(nextActivity.getId());
								.findActivity(targetNodeId);
						TransitionImpl newTransition = currActivity
								.createOutgoingTransition();
						newTransition.setDestination(nextActivityImpl);
						newTransitions.add(newTransition);
					}
					// 完成任务
					List<Task> tasks = taskService.createTaskQuery()
							.processInstanceId(instance.getId())
							.taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
					for (Task task : tasks) {
						taskService.complete(task.getId(), variables);
						historyService.deleteHistoricTaskInstance(task.getId());
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


	@Test
	public void addCourse() {
		List<Course> classList=new ArrayList<>();
        for (int i=0;i<5;i++){
			Course course=new Course();
			course.setType("scoreCourse");
			course.setParentId("df91694dc13cfc670a2071b118e8d7d2");
			if(i==0){
				course.setTitle("语文");
				course.setContent("语文");
				course.setCode("chinese");
			}else if(i==1){
				course.setTitle("数学");
				course.setContent("数学");
				course.setCode("mathematics");
			}else if(i==2){
				course.setTitle("英语");
				course.setContent("英语");
				course.setCode("english");
			}else if(i==3){
				course.setTitle("理综");
				course.setContent("理综");
				course.setCode("science");
			}else if(i==4){
				course.setTitle("文综");
				course.setContent("文综");
				course.setCode("humanities");
			}
			course.setSortBy(i+1);
			course.setStatus("1");
			course.setCreateBy("1");
			classList.add(course);
		}

		classList.stream().forEach(classs -> {
			courseService.getBaseMapper().insert(classs);
		});
	}

	@Test
	public void send() {
		Map<String,Object> map=new HashMap<>();
		map.put("name","lisi");
		map.put("age",18);
		map.put("birthday","1998-12-23");
		rabbitTemplate.convertAndSend("myExchange", "myRoutingKey", map);
	}
	@Test
	public void main() throws IOException, TimeoutException {
		//创建连接工厂
		ConnectionFactory factory = new ConnectionFactory();

		//设置RabbitMQ相关信息
		factory.setHost("127.0.0.1");
		factory.setUsername("admin");
		factory.setPassword("admin");
		factory.setPort(5672);

		//创建一个新的连接
		Connection connection = factory.newConnection();

		//创建一个通道
		Channel channel = connection.createChannel();

		// 声明一个队列
		channel.queueDeclare("rabbitMQ_test2", false, false, false, null);

		//发送消息到队列中
		String message = "Hello RabbitMQ";
		channel.basicPublish("", "rabbitMQ_test2", null, message.getBytes("UTF-8"));
		System.out.println("Producer Send +'" + message + "'");

		//关闭通道和连接
		channel.close();
		connection.close();
	}

}
