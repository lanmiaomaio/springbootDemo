package com.example.springbootdemo.mapper.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.activiti.engine.impl.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liya test
 * @since 2023-03-15
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> userList(@Param("page") IPage page,@Param("user") SysUser user);

    //导出
    List<SysUser> userList(@Param("user") SysUser user);
}
