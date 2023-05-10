package com.example.blogapi.vo.params;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ApplyDocParam {

    /**
     * 文档id
     */
    @JsonSerialize(using= ToStringSerializer.class)
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
     * 是否已经处理 0未处理，1同意，2拒绝
     */
    private Byte finished;

    /**
     * 申请人Id
     */
    public Long applyUserId;

    /**
     * 申请人名
     */
    public String applyUserName;

    /**
     * 更新时间
     */
    public Long updateTime;
}
