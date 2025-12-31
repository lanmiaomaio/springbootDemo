package com.example.springbootdemo.model.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.sql.Blob;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2023-03-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class SysUser extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    private Integer age;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 头像
     */
    private byte[] image;

    /**
     * 部门
     */
    private String deptId;


    private String positionId;

    private String grade;

    private String classs;

    @TableField(exist = false)
    private String gradeName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String gradeClassName;


    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String positionName;

    @TableField(exist = false)
    private List<SysMenu> permissionList;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private String gender;

    /**
     * 状态
     */
    private String status;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time",fill= FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String token;

    @TableField(exist = false)
    private String[] roleIds;

    @TableField(exist = false)
    private String roleNames;

    @TableField(exist = false)
    private String statusName;

    @TableField(exist = false)
    private String genderName;

    @TableField(exist = false)
    private String captcha;

    @TableField(exist = false)
    private String captchaKey;

}
