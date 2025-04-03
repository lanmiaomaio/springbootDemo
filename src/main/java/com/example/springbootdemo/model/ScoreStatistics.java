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

    private String semesterId;

    private String semester;

    private int classCount;

    private int userCount;

    private int chineseCount;
    private int mathematicsCount;
    private int englishCount;

    private int physicsCount;
    private int chemistryCount;
    private int organismCount;

    private int historyCount;
    private int politicsCount;
    private int geographyCount;

    private int scienceCount;
    private int humanitiesCount;


    private int category1Count;
    private int category2Count;
    private int category3Count;
    private int category4Count;

    private String categoryId;
    private String categoryName;
}
