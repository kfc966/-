package com.example.blogapi.vo;

import lombok.Data;

@Data
public class DocDownLoadVo {
    private byte[] content;
    private String docTitle;

}
