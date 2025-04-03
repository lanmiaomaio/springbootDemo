package com.example.springbootdemo.service.impl;

import com.example.springbootdemo.model.ScoreUser;
import com.example.springbootdemo.mapper.ScoreUserMapper;
import com.example.springbootdemo.service.IScoreUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2025-03-14
 */
@Service
public class ScoreUserServiceImpl extends ServiceImpl<ScoreUserMapper, ScoreUser> implements IScoreUserService {

}
