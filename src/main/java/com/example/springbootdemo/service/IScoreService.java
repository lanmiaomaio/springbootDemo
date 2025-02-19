package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springbootdemo.model.Score;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.model.ScoreStatistics;
import com.example.springbootdemo.model.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liya test
 * @since 2025-02-18
 */
public interface IScoreService extends IService<Score> {

  IPage<Score> getPage(int pageNum, int pageSize, Score score);

  List<User> getList();

  int add(Score score);

  boolean edit(Score score);

  Score one(String id);

  boolean del(String id);


  void importList(MultipartFile file, Score score) throws Exception;

  void exportList(HttpServletResponse response,Score score) throws IOException;

  List<ScoreStatistics> gradeStatistics(Score score);

  List<ScoreStatistics> classStatisticsList(Score score);

}
