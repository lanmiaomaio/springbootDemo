package com.example.springbootdemo.model.excelVo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2025-02-18
 */
@Data
public class ScoreExcel {


    @Excel(name="姓名")
    private String username;

   @Excel(name = "学期")
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

}
