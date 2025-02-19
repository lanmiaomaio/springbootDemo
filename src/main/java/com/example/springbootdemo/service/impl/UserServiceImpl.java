package com.example.springbootdemo.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.model.User;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.model.excelVo.UserExcel;
import com.example.springbootdemo.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootdemo.service.system.ISysDictionaryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liya test
 * @since 2024-06-06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private ISysDictionaryService dictionaryService;

    @Override
    public User findByUserName(String userName) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getName,userName);
        return baseMapper.selectOne(userLambdaQueryWrapper);
    }

    @Override
    public IPage<User> getPage(int pageNum, int pageSize, User user) {
        IPage<User> page=new Page<>(pageNum,pageSize);
        IPage<User> userIPage = baseMapper.userList(page, user);

        return userIPage;
    }

    @Override
    @Transactional
    public boolean edit(User user) {
        int i = baseMapper.updateById(user);
        if(i==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    @Transactional
    public boolean add(User user) {
        int insert = baseMapper.insert(user);
        if(insert==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public User one(String id) {
        User user = baseMapper.selectById(id);
        if(StringUtils.isNotBlank(user.getGrade())){
            user.setGradeName(dictionaryService.one(user.getGrade()).getName());
        }
        if(StringUtils.isNotBlank(user.getClasss())){
            user.setClassName(dictionaryService.one(user.getClasss()).getName());
        }
        return user;
    }

    @Override
    public boolean del(String id) {
        int i = baseMapper.deleteById(id);
        if(i==0){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void exportList(HttpServletResponse response, User user) throws IOException {
        List<User> userList = baseMapper.userList(user);
        List<UserExcel> userExcelList = BeanUtil.copyToList(userList, UserExcel.class);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("用户列表.xlx", "UTF-8"));

        ServletOutputStream out = response.getOutputStream();


        //设置excel参数
        ExportParams params = new ExportParams();
        //设置sheet名名称
        params.setSheetName("用户列表");
        //设置标题
        params.setTitle("用户信息表");

        params.setType(ExcelType.HSSF);

        Workbook workbook = ExcelExportUtil.exportExcel(params, UserExcel.class, userExcelList);
        workbook.write(out);
    }
}
