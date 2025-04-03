package com.example.springbootdemo.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.system.SysMenu;
import com.example.springbootdemo.model.system.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2023-03-15
 */
public interface ISysUserService extends IService<SysUser> {
    SysUser findByUserName(String userName);

    IPage<SysUser> getPage(int pageNum,int pageSize,SysUser sysUser);

    boolean edit(SysUser sysUser);

    boolean add(SysUser sysUser);

    SysUser one(String id);

    boolean del(String id);

    String selectNameBatchIds(String[] idList);

    void exportList(HttpServletResponse response,SysUser sysUser) throws IOException;

}
