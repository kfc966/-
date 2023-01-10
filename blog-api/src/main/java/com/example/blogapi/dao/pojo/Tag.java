package com.example.blogapi.dao.pojo;

import lombok.Data;
//data注解可以不用写get，set方法
@Data
public class Tag {
    private Long id;

    private String avatar;

    private String tagName;
}
