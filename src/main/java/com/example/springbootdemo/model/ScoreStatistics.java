package com.example.springbootdemo.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ScoreStatistics extends Model {

    private String gradeId;

    private String classsId;

    private String classs;

    private String grade;

    private int classCount;

    private int userCount;

    private int csoCount;

    private int msoCount;

    private int esoCount;

    private int gssoCount;

    private int cstCount;

    private int mstCount;

    private int estCount;

    private int gsstCount;

}
