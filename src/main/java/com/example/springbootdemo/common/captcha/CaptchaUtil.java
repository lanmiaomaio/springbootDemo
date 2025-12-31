package com.example.springbootdemo.common.captcha;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * 登录验证码生成工具类
 */
public class CaptchaUtil {
    // 验证码宽高
    private static final int WIDTH = 120;
    private static final int HEIGHT = 50;
    // 验证码长度
    private static final int CODE_LENGTH = 4;
    private static final Random random = new Random();
    // 字符集（排除易混淆字符）
    private static final String CODE_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 生成验证码（含字体大小配置）
     */
    public static BufferedImage createImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 1. 填充背景
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 2. 绘制干扰元素（先画干扰，避免遮挡文字）
        drawInterference(g2d);

        // 4. 设置字体（核心：指定字体大小）
        setCaptchaFont(g2d);

        // 5. 绘制验证码文字
        drawCodeText(g2d, code);

        // 6. 释放资源并输出
        g2d.dispose();
        return image;
    }

    /**
     * 核心：设置验证码字体（包含字体大小配置）
     */
    private static void setCaptchaFont(Graphics2D g2d) {
        // 字体名称数组（随机选择，增强多样性）
        String[] fontNames = {"Arial", "Verdana", "Courier New", "SimHei", "Microsoft YaHei"};
        // 字体大小：取图片高度的70%（合理值，避免溢出/过小）
        int fontSize = (int) (HEIGHT * 0.7);
        // 随机选择字体名称，设置粗体+指定字体大小
        Font font = new Font(fontNames[random.nextInt(fontNames.length)], Font.BOLD, fontSize);
        g2d.setFont(font);
    }

    // 生成验证码文本
    public static String createText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHAR.charAt(random.nextInt(CODE_CHAR.length())));
        }
        return sb.toString();
    }

    // 绘制干扰线和噪点
    private static void drawInterference(Graphics2D g2d) {
        // 干扰线
        g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
        for (int i = 0; i < 6; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        // 噪点
        for (int i = 0; i < 100; i++) {
            g2d.fillRect(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
    }

    // 绘制验证码文字（分散排列，避免重叠）
    private static void drawCodeText(Graphics2D g2d, String code) {
        // 单个字符的横向间距（根据宽度和长度计算）
        int charSpacing = (WIDTH - 20) / CODE_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            // 随机文字颜色（深色，保证可读性）
            g2d.setColor(new Color(20 + random.nextInt(120), 20 + random.nextInt(120), 20 + random.nextInt(120)));
            // 随机倾斜（增强安全性）
            int angle = random.nextInt(40) - 20;
            g2d.rotate(Math.toRadians(angle), 15 + i * charSpacing, HEIGHT / 2);
            // 绘制字符（垂直居中：HEIGHT/2 + fontSize/4，优化文字显示位置）
            g2d.drawString(String.valueOf(code.charAt(i)), 15 + i * charSpacing, HEIGHT / 2 + g2d.getFont().getSize() / 4);
            // 恢复旋转
            g2d.rotate(-Math.toRadians(angle), 15 + i * charSpacing, HEIGHT / 2);
        }
    }
}