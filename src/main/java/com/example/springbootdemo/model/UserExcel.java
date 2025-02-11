package com.example.springbootdemo.model;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

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
public class UserExcel {


    /**
     * 姓名
     */
    @Excel(name = "姓名",width = 15)
    private String name;


    @Excel(name = "年龄")
    private Integer age;


    /**
     * 手机号码
     */
    @Excel(name = "手机号码",width = 20)
    private String phone;

    /**
     * 生日
     */
    @Excel(name = "生日",format="yyyy-MM-dd",width = 20)
    private LocalDateTime birthday;

    /**
     * 地址
     */
    @Excel(name = "地址")
    private String address;

    /**
     * 班级
     */
    @Excel(name = "班级")
    private String grade;


    /**
     * 性别
     */
    @Excel(name = "性别", replace = {"男_1", "女_2"})
    private String gender;



    @Excel(name = "创建时间",format="yyyy-MM-dd HH:mm:ss",width = 20)
    private Date createTime;

}
