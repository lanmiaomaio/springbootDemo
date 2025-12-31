package com.example.springbootdemo.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.WxUser;
import com.example.springbootdemo.service.IWxUserService;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.shiro.web.util.RedirectView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/front/wx")
public class WeChatController {

    private static final String TOKEN = "b6d6defe2a3849988404db7e5ce70b9f";

    @Resource
    private WxMpService wxMpService;

    @Autowired
    private IWxUserService wxUserService;

    @Value("${wx.appid}") // 网站应用AppID（开放平台获取）
    private String appId;

    @Value("${wx.appsecret}")
    private String appSecret;
    @Value("${wx.redirecturi}")
    private String redirectUri;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 生成微信扫码登录链接
     */
    @GetMapping("/getQrUrl")
    public ResponseBo getQrUrl() {
        // 1. 生成随机state（防CSRF）
        // 构造授权URL
        String qrUrl = wxMpService.getOAuth2Service().buildAuthorizationUrl(
                redirectUri,
                "snsapi_userinfo", // 授权模式：手动授权（静默授权改为snsapi_base）
                "STATE_123" // 状态参数，可自定义，用于防跨站请求伪造
        );
        return ResponseBo.ok(0,"成功",qrUrl);
    }

    @GetMapping("/getAccessToken")
    public ResponseBo getAccessToken() {
      String aa="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appId+"&secret="+appSecret ;
        String forObject = restTemplate.getForObject(aa, String.class);
        return ResponseBo.ok(0,"成功",forObject);
    }

    /**
     * 獲取用戶信息
     * @param wxUserId
     * @return
     */
    @GetMapping("/getWxUserInfo")
    public ResponseBo getWxUserInfo(String wxUserId) {
        WxUser wxUser = wxUserService.getById(wxUserId);
        return ResponseBo.ok(wxUser);
    }

    @GetMapping("/getCreateMenu")
    public ResponseBo getCreateMenu() {
        try {
            // 1. 构造一级菜单按钮列表
            List<WxMenuButton> buttonList = new ArrayList<>();

            // 按钮1：点击事件（type=click）
            WxMenuButton btn1 = new WxMenuButton();
            btn1.setType("click");
            btn1.setName("测试按钮");
            btn1.setKey("test_btn"); // 事件标识，需自定义（长度≤128）
            buttonList.add(btn1);

            // 按钮2：跳转链接（type=view）
            WxMenuButton btn2 = new WxMenuButton();
            btn2.setType("view");
            btn2.setName("跳转百度");
            btn2.setUrl("https://www.baidu.com"); // 跳转链接（需是公网HTTPS地址）
            buttonList.add(btn2);

            // 按钮3：二级子菜单
            WxMenuButton parentBtn = new WxMenuButton();
            parentBtn.setName("子菜单");
            List<WxMenuButton> subBtnList = new ArrayList<>();
            // 子按钮1：点击事件
            WxMenuButton subBtn1 = new WxMenuButton();
            subBtn1.setType("click");
            subBtn1.setName("子按钮1");
            subBtn1.setKey("sub_btn1");
            subBtnList.add(subBtn1);
            // 子按钮2：跳转链接
            WxMenuButton subBtn2 = new WxMenuButton();
            subBtn2.setType("view");
            subBtn2.setName("子按钮2");
            subBtn2.setUrl("http://192.168.1.7:8080/theatre/index");
            subBtnList.add(subBtn2);
            parentBtn.setSubButtons(subBtnList);
            buttonList.add(parentBtn);

            // 2. 构造菜单对象
            WxMenu menu = new WxMenu();
            menu.setButtons(buttonList);

            // 3. 调用SDK接口创建菜单
            wxMpService.getMenuService().menuCreate(menu);
            return ResponseBo.ok();
        } catch (WxErrorException e) {
            return ResponseBo.error();
        }

    }

    @GetMapping("/callback")
    public void callback(HttpServletResponse response,
                         @RequestParam("code") String code,
                         @RequestParam("state") String state
    ) {
        try {
            // 1. 用code换取access_token和openid
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);

            // 2. 用access_token获取用户信息（snsapi_userinfo授权才可用）
            WxOAuth2UserInfo userInfo = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
            LambdaQueryWrapper<WxUser> wxUserLambdaQueryWrapper=new LambdaQueryWrapper<>();
            wxUserLambdaQueryWrapper.eq(WxUser::getOpenid,userInfo.getOpenid());
            WxUser one = wxUserService.getOne(wxUserLambdaQueryWrapper);
            String wxUserId="";
            if(one==null){
                WxUser wxUser=new WxUser();
                BeanUtils.copyProperties(userInfo,wxUser);
                wxUserService.save(wxUser);
                wxUserId= wxUser.getId();
            }else{
                wxUserId=one.getId();
            }
            String targetUrl = "http://192.168.1.7:8080/theatre/index?wxUserId="+wxUserId;
            // 手动设置重定向响应头
            response.setStatus(HttpServletResponse.SC_FOUND); // 302 重定向状态码
            response.sendRedirect(targetUrl); // 核心方法：发送重定向
        } catch (WxErrorException | IOException e) {
            e.printStackTrace();

        }
    }

    // 接口路径必须和配置页的URL路径一致（如URL是https://xxx.com/wx/verify，这里就映射/wx/verify）
    @GetMapping("/verify")
    public String verifyWxToken(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr
    ) {
        try {
            // 1. 按Token、timestamp、nonce排序
            String[] arr = new String[]{TOKEN, timestamp, nonce};
            Arrays.sort(arr);
            // 2. 拼接成字符串
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }
            // 3. SHA1加密（转小写，避免大小写不一致）
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
            StringBuilder sha1 = new StringBuilder();
            for (byte b : digest) {
                sha1.append(String.format("%02x", b)); // 小写输出
            }
            // 4. 对比签名，一致则返回echostr
            if (sha1.toString().equals(signature)) {
                return echostr; // 必须原样返回，不能加任何额外字符
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // 验证失败返回空
    }
}
