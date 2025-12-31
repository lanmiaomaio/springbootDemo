package com.example.springbootdemo.service.impl;

import com.example.springbootdemo.model.WxUser;
import com.example.springbootdemo.mapper.WxUserMapper;
import com.example.springbootdemo.service.IWxUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2025-12-24
 */
@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {

}
