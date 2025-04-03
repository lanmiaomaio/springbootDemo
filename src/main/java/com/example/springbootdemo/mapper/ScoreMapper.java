package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.Score;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.ScoreCategory;
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

    IPage<ScoreCategory> selectScoreCategoryPage(@Param("page") IPage page, @Param("score") Score score, @Param("classArray") String[] classs);

    IPage<Score> selectPage(@Param("page") IPage page, @Param("score") Score score,@Param("classArray") String[] classs);

    List<Score> selectPage(@Param("score") Score score,@Param("classArray") String[] classs);

    List<ScoreStatistics> gradeStatistics(@Param("score") Score score,@Param("classArray") String[] classs);

    List<ScoreStatistics> gradeStatistics1(@Param("score") Score score,@Param("classArray") String[] classs);

    List<ScoreStatistics> classStatistics(@Param("score") Score score,@Param("classArray") String[] classs);

    List<ScoreStatistics> classDetailStatistics(@Param("score") Score score,@Param("classArray") String[] classs);


    IPage<Score> selectStatisticsDetail(@Param("page") IPage page, @Param("score") Score score,@Param("classArray") String[] classs);


}
