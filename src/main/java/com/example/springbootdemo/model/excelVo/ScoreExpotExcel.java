package com.example.springbootdemo.model.excelVo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2025-02-18
 */
@Data
public class ScoreExpotExcel {

    @Excel(name="考试类型",width = 15)
    private String scoreCategory;

    @Excel(name="班级",width = 15)
    private String gradeClassName;

    @Excel(name="考试时间",format="yyyy-MM-dd",width = 18)
    private Date scoreTime;

    @Excel(name="学号",width = 18)
    private String userNo;

    @Excel(name="姓名")
    private String username;

    @Excel(name="性别")
    private String genderName;



   @Excel(name = "学期",width = 15)
   private String semester;

    /**
     * 语文
     */
    @Excel(name = "语文",width = 15)
    private Integer chinese;

    /**
     * 数学
     */
    @Excel(name = "数学",width = 15)
    private Integer mathematics;

    /**
     * 英语
     */
    @Excel(name = "英语",width = 15)
    private Integer english;

    /**
     * 物理
     */
    @Excel(name = "物理",width = 15)
    private Integer physics;

    /**
     * 化学
     */
    @Excel(name = "化学",width = 15)
    private Integer chemistry;

    /**
     * 生物
     */
    @Excel(name = "生物",width = 15)
    private Integer organism;


    /**
     * 历史
     */

    @Excel(name = "历史",width = 15)
    private Integer history;

    /**
     * 政治
     */
    @Excel(name = "政治",width = 15)
    private Integer politics;

    /**
     * 地理
     */

    @Excel(name = "地理",width = 15)
    private Integer geography;

    @Excel(name = "总分",width = 15)
    private Integer totalScore;

    @Excel(name = "录入时间",format="yyyy-MM-dd HH:mm:ss",width = 20)
    private Date createTime;

}
