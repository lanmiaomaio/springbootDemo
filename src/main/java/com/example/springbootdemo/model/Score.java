package com.example.springbootdemo.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;
import com.example.springbootdemo.model.system.SysDictionary;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2025-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Score extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

    @TableField(exist = false)
    private String grade;

    @TableField(exist = false)
    private String classs;

    /**
     * 语文
     */
    private Integer chinese;

    /**
     * 数学
     */
    private Integer mathematics;

    /**
     * 英语
     */
    private Integer english;

    /**
     * 物理
     */
    private Integer physics;

    /**
     * 化学
     */
    private Integer chemistry;

    /**
     * 生物
     */
    private Integer organism;

    /**
     * 理综
     */
    private Integer science;

    /**
     * 历史
     */
    private Integer history;

    /**
     * 政治
     */
    private Integer politics;

    /**
     * 地理
     */
    private Integer geography;

    /**
     * 文综
     */
    private Integer humanities;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 学期
     */
    @TableField(exist = false)
    private String semester;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String gradeName;

    @TableField(exist = false)
    private String genderName;

    @TableField(exist = false)
    private String isPass;


    @TableField(exist = false)
    private String userNo;

    @TableField(exist = false)
    private String age;

    @TableField(exist = false)
    private String phone;

    @TableField(exist = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime userCreateTime;


    @TableField(exist = false)
    private String userId;
    @TableField(exist = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date scoreTime;
    @TableField(exist = false)
    private String scoreCategory;

    @TableField(exist = false)
    private String scoreCategoryId;

    @TableField(exist = false)
    private String gradeClassName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String type;

    @TableField(exist = false)
    private String scienceHumanitiesClass;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scoreCreateTime;

    @TableField(exist = false)
    private List<Course> scoreCourseList;

    @TableField(exist = false)
    private List<ScoreCourse> scoreCourseImportList;

    @TableField(exist = false)
    private List<SysDictionary> scoreCategoryList;



}
