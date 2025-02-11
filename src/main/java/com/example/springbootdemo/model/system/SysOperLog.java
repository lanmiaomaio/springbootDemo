package com.example.springbootdemo.model.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2023-05-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class SysOperLog extends Model {

    private static final long serialVersionUID = 1L;

    /**
     * 操作状态
     */
    @TableId(value = "id", type = IdType.UUID)
    private String id;

    /**
     * 模块标题
     */
    private String title;


    private String method;

    private String ipAddress;

    /**
     * 方法名称
     */
    private String uri;

    /**
     * 操作状态（0：正常，1：异常）
     */
    private String status;

    /**
     * 参数
     */
    private String resParam;

    private String reqParam;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    private String userId;

    private String username;


}
