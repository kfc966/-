package com.example.blogapi.service;

import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.LoginParam;

public interface LoginService {
    Result login(LoginParam loginParam);

    SysUser checkToken(String token);

    Result logout(String token);

    Result register(LoginParam loginParam);
}
