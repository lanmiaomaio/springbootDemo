package com.example.springbootdemo.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2023-04-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TaskVo extends Model {

    private static final long serialVersionUID = 1L;

    private String id;


    private String userName;

    //流程名称
    private String processDeptName;


    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime endDate;

    //当前审批人
    private String approvalUserName;


    /**
     * 流程状态（0：待提交；1：审批中；2：审批通过；3：审批不通过）
     */
    private String processStatus;
    /**
     * 流程实例id
     */
    private String processDeptId;

    /**
     * 申请人用户id
     */
    private String userId;

    /**
     * 请假申请原因
     */
    private String reason;

    private String approvalUserId;


    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


    private String processInstanceId;


}
