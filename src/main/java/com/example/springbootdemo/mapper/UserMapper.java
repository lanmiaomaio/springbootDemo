package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liya test
 * @since 2024-06-06
 */
public interface UserMapper extends BaseMapper<User> {


    IPage<User> userList(@Param("page") IPage page, @Param("user") User user);

    //导出
    List<User> userList(@Param("user") User user);
}
