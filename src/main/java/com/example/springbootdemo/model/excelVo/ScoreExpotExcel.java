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


    @Excel(name="姓名")
    private String username;

    @Excel(name="性别")
    private String genderName;

    @Excel(name="班级",width = 15)
    private String gradeName;

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
     * 理综
     */
    @Excel(name = "理综",width = 15)
    private Integer generalScience;

    @Excel(name = "录入时间",format="yyyy-MM-dd HH:mm:ss",width = 20)
    private Date createTime;

}
