package com.example.blogapi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUserVo {

    private String id;

    private String account;

    private String nickname;

    private String avatar;
}