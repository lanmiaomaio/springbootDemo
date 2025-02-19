package com.example.springbootdemo.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;

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
 * @since 2023-04-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("`leave`")
public class Leave extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime endDate;

    /**
     * 流程状态（0：待提交；1：审批中；2：审批通过；3：审批不通过）
     */
    private String processStatus;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time",fill= FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String approvalUserName;

    @TableField(exist = false)
    private String processDeptName;

    @TableField(exist = false)
    private String taskId;

    private String processInstanceId;

    private Float day;

    @TableField(exist = false)
    private String type;

}
