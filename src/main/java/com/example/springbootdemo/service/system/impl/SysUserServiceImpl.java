package com.example.springbootdemo.service.system.impl;

import cn.afterturn.easypoi.entity.ImageEntity;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.model.system.*;
import com.example.springbootdemo.mapper.system.SysUserMapper;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import com.example.springbootdemo.service.system.ISysRoleService;
import com.example.springbootdemo.service.system.ISysUserRoleService;
import com.example.springbootdemo.service.system.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2023-03-15
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysDictionaryService sysDictionaryService;

    @Autowired
    private IdentityService identityService;

    @Override
    public SysUser findByUserName(String userName) {
        LambdaQueryWrapper<SysUser> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(SysUser::getUsername,userName).eq(SysUser::getStatus,"1");
        return baseMapper.selectOne(userLambdaQueryWrapper);
    }

    @Override
    public IPage<SysUser> getPage(int pageNum, int pageSize,SysUser sysUser) {
        IPage<SysUser> page=new Page<>(pageNum,pageSize);
        IPage<SysUser> userIPage = baseMapper.userList(page, sysUser);

        return userIPage;
    }

    @Override
    @Transactional
    public boolean edit(SysUser sysUser) {
        LambdaQueryWrapper<SysUserRole> userRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleLambdaQueryWrapper.eq(SysUserRole::getUserId,sysUser.getId());
        sysUserRoleService.getBaseMapper().delete(userRoleLambdaQueryWrapper);
        int i = baseMapper.updateById(sysUser);
        if(i==1){
            if(StringUtils.isNotBlank(sysUser.getStatus())&&"1".equals(sysUser.getStatus())){
                identityService.deleteUser(sysUser.getId());
                User user = identityService.newUser(sysUser.getId());
                user.setFirstName(sysUser.getUsername());
                user.setLastName(sysUser.getRealName());
                identityService.saveUser(user);
            }else{
                identityService.deleteUser(sysUser.getId());
            }
        }
        if(sysUser.getRoleIds()!=null){
            for (String roleId:sysUser.getRoleIds()){
                SysUserRole userRole=new SysUserRole();
                userRole.setUserId(sysUser.getId());
                userRole.setRoleId(roleId);
                sysUserRoleService.save(userRole);
                if(StringUtils.isNotBlank(sysUser.getStatus())&&"1".equals(sysUser.getStatus())) {
                    identityService.createMembership(sysUser.getId(),roleId);
                }
            }
        }
        if(i==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    @Transactional
    public boolean add(SysUser sysUser) {
        int insert = baseMapper.insert(sysUser);
        if(insert==1){
            if(StringUtils.isNotBlank(sysUser.getStatus())&&"1".equals(sysUser.getStatus())){
                User user = identityService.newUser(sysUser.getId());
                user.setFirstName(sysUser.getUsername());
                user.setLastName(sysUser.getRealName());
                identityService.saveUser(user);
            }
        }
        for (String roleId:sysUser.getRoleIds()){
            SysUserRole userRole=new SysUserRole();
            userRole.setUserId(sysUser.getId());
            userRole.setRoleId(roleId);
            sysUserRoleService.save(userRole);
            if(StringUtils.isNotBlank(sysUser.getStatus())&&"1".equals(sysUser.getStatus())){
                identityService.createMembership(sysUser.getId(),roleId);

            }
        }
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public SysUser one(String id) {
        SysUser sysUser = baseMapper.selectById(id);
        if(sysUser!=null){
            if(StringUtils.isNotBlank(sysUser.getDeptId())){
                sysUser.setDeptName(sysDictionaryService.one(sysUser.getDeptId()).getName());
            }
            if(StringUtils.isNotBlank(sysUser.getPositionId())){
                sysUser.setPositionName(sysDictionaryService.one(sysUser.getPositionId()).getName());
            }
        }
        LambdaQueryWrapper<SysUserRole> userRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleLambdaQueryWrapper.eq(SysUserRole::getUserId,id);
        List<SysUserRole> list = sysUserRoleService.list(userRoleLambdaQueryWrapper);
        String[] roleArray=list.stream().map(sysUserRole -> sysUserRole.getRoleId()).toArray(String[]::new);
        if(roleArray.length!=0){
            List<SysRole> sysRoles = sysRoleService.getBaseMapper().selectBatchIds(Arrays.asList(roleArray));
            String roleNames = sysRoles.stream().map(role -> role.getRoleName()).collect(Collectors.joining("，"));
            sysUser.setRoleNames(roleNames);
            sysUser.setRoleIds(roleArray);
        }
        return sysUser;
    }

    @Override
    public boolean del(String id) {
        int i = baseMapper.deleteById(id);
        if(i==1){
            LambdaQueryWrapper<SysUserRole> userRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
            userRoleLambdaQueryWrapper.eq(SysUserRole::getUserId,id);
            sysUserRoleService.remove(userRoleLambdaQueryWrapper);
            identityService.deleteUser(id);
        }
        if(i==0){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void exportList(HttpServletResponse response,SysUser sysUser) throws IOException {
        List<SysUser> sysUserList = baseMapper.userList(sysUser);
        List<SysUserExcel> sysUserExcelList = BeanUtil.copyToList(sysUserList, SysUserExcel.class);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("系统用户.xlx", "UTF-8"));

        ServletOutputStream out = response.getOutputStream();


        //设置excel参数
        ExportParams params = new ExportParams();
        //设置sheet名名称
        params.setSheetName("系统用户");
        //设置标题
        params.setTitle("系统用户信息表");

        params.setType(ExcelType.HSSF);

        Workbook workbook = ExcelExportUtil.exportExcel(params, SysUserExcel.class, sysUserExcelList);
        workbook.write(out);
    }

}
