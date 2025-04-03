package com.example.springbootdemo.model;

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
 * @since 2025-03-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ScoreCategory extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

    private String semester;

    private String userId;

    private String scoreCategory;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date scoreTime;

    @TableField(exist = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date scoreCreateTime;

    @TableField(exist = false)
    private String scoreCategoryName;


    @TableField(exist = false)
    private List<Score> children;

    @TableField(exist = false)
    private String[] classArray;

    @TableField(exist = false)
    private String classs;
    @TableField(exist = false)
    private String grade;

    @TableField(exist = false)
    private String gradeClassName;

    @TableField(exist = false)
    private String type;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

}
