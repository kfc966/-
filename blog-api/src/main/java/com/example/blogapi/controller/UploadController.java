package com.example.blogapi.controller;

import com.example.blogapi.utils.FastdfsUtils;
import com.example.blogapi.vo.DocDownLoadVo;
import com.example.blogapi.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {

    @Value("${host}")
    private String host;

    @Value("${server.port}")
    private String port;

    @Resource
    private FastdfsUtils fastdfsUtils;
    @PostMapping
    public Result upload(@RequestParam("image")MultipartFile multipartFile, HttpServletRequest request)
    {
        try {
            String requestURI = request.getRequestURI();
            String storePath = fastdfsUtils.upload(multipartFile);
            storePath = storePath.replace("/", "+");
            String uri = String.format("%s/%s", requestURI, storePath);
            return Result.success(uri);
        } catch (IOException e) {
            return  Result.fail(20001,"上传失败！");
        }
    }

    @GetMapping ("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) throws IOException {
        id = id.replace("+", "/");
        byte[] bytes = fastdfsUtils.download(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition",
                "attachment;filename=" +  URLEncoder.encode(id,"utf-8"));
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        return  responseEntity;
    }
}
