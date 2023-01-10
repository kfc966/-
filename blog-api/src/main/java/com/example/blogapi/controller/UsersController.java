package com.example.blogapi.controller;

import com.example.blogapi.service.SysUserService;
import com.example.blogapi.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UsersController {
    @Autowired
    private SysUserService sysUserService;
    @GetMapping("currentUser")
    public Result currentUser(@RequestHeader("Authorization") String token)
    {
        return sysUserService.findUserByToken(token);
    }
}
