package com.example.datasourcedemo.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件上传、下载的接口
 *
 * @author bxy
 * @date 2024/7/11 11:31:37
 */
@RestController
public class FileController {

    /**
     * 文件下载
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void downloadFiles(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment;filename=test1.txt");
        response.setContentType("application/octet-stream");
        response.getOutputStream().write("test".getBytes());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    //responseEntity.getBody().writeTo(response.getOutputStream());
    @GetMapping("/download2")
    public ResponseEntity<byte[]> downloadFiles2(HttpServletResponse response) throws IOException {
        // 设置你想要的文件名
        String filename = "test.txt";
        String fileContent = "test";
        byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()));

        return ResponseEntity.ok()
                             .headers(headers)
                             .body(fileBytes);
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public void uploadFiles(@RequestParam("file") MultipartFile file) {
        //todo 处理文件等操作
        System.out.println(file.getSize());
        System.out.println(file.getOriginalFilename());
    }

}
