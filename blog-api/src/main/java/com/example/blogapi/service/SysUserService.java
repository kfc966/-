package com.example.blogapi.service;

import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;

public interface SysUserService {
    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);

    Result findUserByToken(String token);

    SysUser findUserByAccount(String account);

    void save(SysUser sysUser);
    UserVo findUserVoById(Long id);
}
