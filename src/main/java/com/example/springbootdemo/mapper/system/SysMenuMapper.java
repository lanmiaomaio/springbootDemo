package com.example.springbootdemo.mapper.system;

import com.example.springbootdemo.model.system.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liya test
 * @since 2023-03-22
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectLeftMenu(@Param("userId") String currentUserId, @Param("parentId") String parentId);

    List<SysMenu> selectPermission(@Param("menuId") String menuId,@Param("userId") String currentUserId);

}
