package com.example.springbootdemo.common;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FFmpegUtils {
    @Value("${video.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${video.output-path}")
    private String outputPath;

    /**
     * 调用FFmpeg将视频切片为ts，并生成m3u8文件
     * @param inputVideoPath 原始视频文件路径（如 ./upload/test.mp4）
     * @param m3u8FileName 生成的m3u8文件名（如 test.m3u8）
     * @param sliceDuration 分片时长（秒，默认10秒）
     * @throws IOException 执行命令异常
     */
    public String generateM3u8(String inputVideoPath, String m3u8FileName, int sliceDuration, boolean needTranscode) throws IOException, IOException {
        // 1. 创建输出目录（不存在则创建）
        String uuid= UUID.randomUUID().toString().replace("-", "");
        File outputDir = new File(outputPath+uuid);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        long totalStart = System.currentTimeMillis();
        // 2. 构造FFmpeg切片命令
        // 核心命令：ffmpeg -i 输入视频 -c:v h264 -c:a aac -hls_time 10 -hls_list_size 0 -hls_segment_filename 输出ts路径 输出m3u8路径
        String outputM3u8Path = (outputPath+uuid+"\\" )+ File.separator + m3u8FileName;
        String tsSegmentPath = (outputPath+uuid+"\\" )+ File.separator + "%03d.ts"; // ts分片命名格式（000.ts、001.ts...）

        CommandLine cmd = new CommandLine(ffmpegPath);
        // 核心：禁用冗余日志（从源头减少流输出）
        cmd.addArgument("-hide_banner"); // 隐藏启动横幅
        cmd.addArgument("-loglevel");
        cmd.addArgument("error"); // 只输出错误日志（调试时可改为 warning）

        // 新增：全局时间戳修复（所有分支必加）
        cmd.addArgument("-fflags");
        cmd.addArgument("+genpts"); // 强制生成连续时间戳
        cmd.addArgument("-avoid_negative_ts");
        cmd.addArgument("make_zero"); // 修复负时间戳，统一时间基
        // 源视频路径
        cmd.addArgument("-i");
        cmd.addArgument(inputVideoPath);
        // 关键：优先直接拷贝流（不转码），必须转码时才指定编码（或启用硬件加速）
        if (needTranscode) {
            cmd.addArgument("-c:v");
            cmd.addArgument("h264"); // 软件转码（仅当必须时用）
            cmd.addArgument("-g");
            cmd.addArgument("50"); // 关键帧间隔（帧率×2，假设帧率25，即每2秒1个关键帧）
            cmd.addArgument("-c:a");
            cmd.addArgument("aac");
        } else {
            cmd.addArgument("-c");
            cmd.addArgument("copy"); // 直接拷贝流，跳过转码（最快）
            // 新增：拷贝时强制对齐关键帧
            cmd.addArgument("-hls_flags");
            cmd.addArgument("split_by_time");
        }
        // 分片配置
        cmd.addArgument("-hls_time");
        cmd.addArgument("5"); // 固定分片时长为5秒（避免变量导致的时长偏差）
        cmd.addArgument("-hls_list_size");
        cmd.addArgument("0"); // 保留所有分片
        cmd.addArgument("-hls_segment_filename");
        cmd.addArgument(tsSegmentPath);
        // 新增：点播模式（时长固定，无动态更新）
        cmd.addArgument("-hls_playlist_type");
        cmd.addArgument("vod");
        cmd.addArgument(outputM3u8Path);

        // 2. 初始化执行器 + 优化流处理器（不输出到控制台，异步消费日志）
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        // 用 ByteArrayOutputStream 接收日志（不输出到System.out），减少IO阻塞
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(logOutput, logOutput);
        executor.setStreamHandler(streamHandler);

        // 3. 执行 FFmpeg 命令（核心耗时操作）
        long ffmpegStart = System.currentTimeMillis();
        try {
            executor.execute(cmd); // 同步执行（如需异步，结合Spring @Async）
            return uuid;

        } catch (ExecuteException e) {
            throw new RuntimeException("生成m3u8失败", e);
        } catch (IOException e) {
            throw new RuntimeException("生成m3u8失败", e);
        }
    }
}
