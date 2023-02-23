package com.example.blogapi.dao.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Document {
    /**
     * 文档id
     */
    private String id;

    /**
     * 发布者id
     */
    private Long publisherId;

    /**
     * 文档名称
     */
    private String docTitle;

    /**
     * uri
     */
    private String docUri;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 文件所有权
     */
    Byte ownerType;
}
