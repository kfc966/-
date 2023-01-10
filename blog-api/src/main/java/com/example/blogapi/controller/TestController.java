package com.example.blogapi.controller;

import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        System.out.println(UserThreadLocal.get());
        return Result.success(null);
    }
}