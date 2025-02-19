package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.Score;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.ScoreStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liya test
 * @since 2025-02-18
 */
public interface ScoreMapper extends BaseMapper<Score> {

    IPage<Score> selectPage(@Param("page") IPage page, @Param("score") Score score);

    List<Score> selectPage(@Param("score") Score score);

    List<ScoreStatistics> gradeStatistics(@Param("score") Score score);

    List<ScoreStatistics> classStatistics(@Param("score") Score score);

}
