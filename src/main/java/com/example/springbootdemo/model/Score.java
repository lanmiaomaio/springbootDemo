package com.example.springbootdemo.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
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
 * @since 2025-02-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Score extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

    private String grade;

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
     * 理综
     */
    private Integer generalScience;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 学期
     */
    private String semester;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String gradeName;

    @TableField(exist = false)
    private String genderName;


    private String userId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;


}
