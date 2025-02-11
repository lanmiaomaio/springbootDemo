package com.example.springbootdemo.model.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2023-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class SysMenu extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父id
     */
    private String parentId;

    /**
     * 图标
     */
    private String icon;

    private String url;

    /**
     * 类型：0菜单，1按钮，
     */
    private String type;

    /**
     * 状态
     */
    private String status;

    /**
     * 描述
     */
    private String des;

    /**
     * 是否外连接
     */
    private String isLink;

    /**
     * 排序
     */
    private Integer sortBy;

    private String permissionIdentity;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time",fill= FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SysMenu> children;

    @TableField(exist = false)
    private String parentName;


}
