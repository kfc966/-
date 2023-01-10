package com.example.blogapi.controller;

import com.example.blogapi.vo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    @PostMapping
    public Result upload(@RequestParam("image")MultipartFile multipartFile)
    {
        String filename = UUID.randomUUID().toString() + multipartFile.getOriginalFilename();
        File targetFile=new File("D:\\demo\\blog\\blog-api\\uploadfile\\"+filename);
        try {
            multipartFile.transferTo(targetFile);
            return  Result.success(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  Result.fail(20001,"上传失败！");

    }
}
