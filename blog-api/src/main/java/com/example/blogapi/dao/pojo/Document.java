package com.example.blogapi.dao.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@org.springframework.data.elasticsearch.annotations.Document(indexName = "ms_document")
public class Document {
    /**
     * 文件所有权
     */
    @Field(type = FieldType.Byte)
    Byte ownerType;
    /**
     * 文档id
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    /**
     * 发布者id
     */
    @Field(type = FieldType.Long,index = false)
    private Long publisherId;
    /**
     * 发布者名字
     */
    @TableField(exist = false)
    @Field(type = FieldType.Text,analyzer = "ik_max_word", copyTo = "all")
    private String publisher;
    /**
     * 文档名称
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word", copyTo = "all")
    private String docTitle;
    /**
     * uri
     */
    @Field(type = FieldType.Keyword,index = false)
    private String docUri;
    /**
     * 更新时间
     */
    @Field(type = FieldType.Long)
    private Long updateTime;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    @TableField(exist = false)
    private String all;

    @Field(type = FieldType.Text,analyzer = "ik_max_word", copyTo = "all")
    @TableField(exist = false)
    private String content;
}
