package com.example.springbootdemo.common.weixin;


// 必须添加这行导入，否则IDE识别不了WxOpenDefaultConfigImpl
// 同时补充WxOpenService/Impl的导入（若未加）
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WeChatCodeConfig {

    @Value("${wx.appid}")
    private String appId;
    @Value("${wx.appsecret}")
    private String appSecret;
    /**
     * 定义WxMpService bean
     */

    @Bean
    public WxMpService wxMpService() {
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(appId);
        config.setSecret(appSecret);

        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(config);
        return service;
    }
//    @Bean
//    public WxOpenService wxOpenService() {
//        WxOpenConfigStorage config = new WxOpenInMemoryConfigStorage();
//        config.setComponentAppId(appId);
//        config.setComponentAppSecret(appSecret);
//
//        WxOpenService service = new WxOpenServiceImpl();
//        service.setWxOpenConfigStorage(config);
//        return service;
//    }

}
