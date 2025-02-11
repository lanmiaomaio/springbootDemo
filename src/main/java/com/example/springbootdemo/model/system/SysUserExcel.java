package com.example.springbootdemo.model.system;

import cn.afterturn.easypoi.entity.ImageEntity;
import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2023-03-15
 */
@Data
public class SysUserExcel {


    /**
     * 姓名
     */
    @Excel(name = "姓名",width = 15)
    private String realName;

    /**
     * 用户名
     */
    @Excel(name = "用户名",width = 15)
    private String username;

    @Excel(name = "年龄")
    private Integer age;


    /**
     * 手机号码
     */
    @Excel(name = "手机号码",width = 20)
    private String phone;


    /**
     * 性别
     */
    @Excel(name = "性别", replace = {"男_1", "女_2"})
    private String gender;

    /**
     * 部门
     */
    @Excel(name = "部门",width = 15)
    private String deptName;

    @Excel(name = "职位",width = 15)
    private String positionName;

    /**
     * 头像
     * 注解中Excel type：1-文本；2-图片；3-函数；10-数字
     */
//    @Excel(name = "头像", type = 2, width = 10.0, imageType = 2)
//    private byte[] image;




    /**
     * 状态
     */
    @Excel(name = "状态", replace = {"启用_1", "禁用_2"})
    private String status;

    @Excel(name = "创建时间",format="yyyy-MM-dd HH:mm:ss",width = 20)
    private Date createTime;

}
