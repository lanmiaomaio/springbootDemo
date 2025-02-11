package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.system.SysUser;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2024-06-06
 */
public interface IUserService extends IService<User> {


    User findByUserName(String userName);

    IPage<User> getPage(int pageNum, int pageSize, User user);

    boolean edit(User user);

    boolean add(User user);

    User one(String id);

    boolean del(String id);

    void exportList(HttpServletResponse response,User user) throws IOException;
}
