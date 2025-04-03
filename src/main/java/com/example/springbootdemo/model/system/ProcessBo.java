package com.example.springbootdemo.model.system;

import lombok.Data;

@Data
public class ProcessBo {

    private String id;
    private String name;
    private String key;
    private String category;
    private String categoryName;
    private String description;
    private int suspensionState;
    private String suspensionStateName;
    private String deploymentId;

    private String createTime;
}
