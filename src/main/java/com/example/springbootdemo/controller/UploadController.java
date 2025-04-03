package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.pojo.ResponseBo;
import com.example.springbootdemo.model.ProjectFile;
import com.example.springbootdemo.service.IProjectFileService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

@RestController
public class UploadController {

    @Autowired
    private IProjectFileService projectFileService;

    @PostMapping("/upload")
    public ResponseBo upload(MultipartFile file) {
        // 对文件判空
        if (file.isEmpty()) {
            return ResponseBo.error("文件为空");
            // 返回一个 R 类型给前端
            // R 为规定数据格式的统一返回对象
        }
        String originalFileName = file.getOriginalFilename();
        // ↑ 获取文件的名称
        String fileName = System.currentTimeMillis() + "." + originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        // ↑ 以当前时间戳对文件进行重命名，并保存文件名后缀
        String filePath = "D:/home/demo/tempdownload/";
        // ↑ 设定文件的所要上传的服务器路径
        File dist = new File(filePath + fileName);

        // ↓ 对文件路径判空
        if (!dist.getParentFile().exists()) {
            dist.getParentFile().mkdirs();
            // ↑ 若文件路径不存在则创建
        }
        try {
            // ↓ 上传文件
            file.transferTo(dist);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBo.error("上传失败");
        }

        return ResponseBo.ok(0,"上传成功",fileName);
    }



    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download/{name}")
    public void download(@PathVariable("name")String name, HttpServletResponse response){
        //输入流，通过输入流读取文件内容
        try {
            String filePath = "D:/home/demo/tempdownload/";
            FileInputStream fileInputStream = new FileInputStream(new File(filePath+name));
            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/download1/{id}")
    public void download1 (@PathVariable("id") String id,HttpServletResponse response) throws IOException {
         ProjectFile projectFile = projectFileService.getById(id);

        //输入流，通过输入流读取文件内容
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + encodeFileName(projectFile.getFileName()));
            OutputStream out = response.getOutputStream();
            out.write(projectFile.getFileUrl());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 处理中文文件名编码问题‌:ml-citation{ref="8" data="citationList"}
    private String encodeFileName(String name) throws UnsupportedEncodingException {
        return new String(name.getBytes("GBK"), "ISO-8859-1");
    }

    /**
     * 上传获取文件流
     * @param file
     * @return
     */
    @PostMapping("/upload1")
    public ResponseBo upload1(MultipartFile file) throws IOException {
        // 对文件判空
        if (file.isEmpty()) {
            return ResponseBo.error("文件为空");
            // 返回一个 R 类型给前端
            // R 为规定数据格式的统一返回对象
        }
        String originalFileName = file.getOriginalFilename();
        // ↑ 获取文件的名称
        String fileName = System.currentTimeMillis() + "." + originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        // ↑ 以当前时间戳对文件进行重命名，并保存文件名后缀
        String filePath = "D:/home/demo/tempdownload/";
        // ↑ 设定文件的所要上传的服务器路径
        File dist = new File(filePath + fileName);

        // ↓ 对文件路径判空
        if (!dist.getParentFile().exists()) {
            dist.getParentFile().mkdirs();
            // ↑ 若文件路径不存在则创建
        }
        try {
            // ↓ 上传文件
            file.transferTo(dist);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBo.error("上传失败");
        }

        FileInputStream inputstream = new FileInputStream(new File(filePath+fileName));
        byte[] byt= IOUtils.toByteArray(inputstream);
        return ResponseBo.ok(0,"上传成功",byt);
    }

}
