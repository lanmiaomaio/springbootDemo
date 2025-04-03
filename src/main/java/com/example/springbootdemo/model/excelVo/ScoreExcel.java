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

    @Excel(name = "学号")
    private String userNo;

    @Excel(name="姓名")
    private String username;

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

    @Excel(name = "理综",width = 15)
    private Integer science;

    @Excel(name = "文综",width = 15)
    private Integer humanities;
}
