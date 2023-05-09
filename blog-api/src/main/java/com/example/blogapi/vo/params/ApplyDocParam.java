package com.example.blogapi.vo.params;

import lombok.Data;

@Data
public class ApplyDocParam {

    /**
     * 文档id
     */
    private Long docId;

    private String docTitle;

    private String docDesc;

    /**
     * 作者id
     */
    private Long publisherId;

    /**
     * 申请理由
     */
    private String reason;

    /**
     * 是否已经处理
     */
    private Boolean finished;

    /**
     * 申请人
     */
    public Long applyUserId;

    /**
     * 更新时间
     */
    public Long updateTime;
}
