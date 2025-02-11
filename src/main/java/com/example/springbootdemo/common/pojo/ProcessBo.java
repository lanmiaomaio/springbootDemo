package com.example.springbootdemo.common.pojo;

import lombok.Data;

@Data
public class ProcessBo {

    private String id;
    private String name;
    private String key;
    private String description;
    private String suspensionState;
    private String deploymentId;

    private String createTime;
}
