package com.example.springbootdemo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author liya test
 * @since 2025-09-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserCourse extends Model {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.UUID)
    private String id;

    private String userId;

    private String gradeId;

    private String classsId;



}
