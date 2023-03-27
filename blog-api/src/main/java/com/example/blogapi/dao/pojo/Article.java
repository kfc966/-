package com.example.blogapi.dao.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

//mybatis-plus 默认使用类名去查找表
//@TableName("ms_article")
@Data
@Document(indexName = "ms_article")
public class Article {
    public static final int Article_TOP = 1;

    public static final int Article_Common = 0;

    @Id
    @Field(type = FieldType.Keyword)
    private Long id;

    @Field(type = FieldType.Text,analyzer = "ik_max_word", copyTo = "all")
    private String title;
    @Field(type = FieldType.Text,analyzer = "ik_max_word", copyTo = "all")
    private String summary;
    @Field(type = FieldType.Integer)
    private Integer commentCounts;

    @Field(type = FieldType.Integer)
    private Integer viewCounts;

    /**
     * 作者id
     */
    @Field(type = FieldType.Long)
    private Long authorId;
    /**
     * 内容id
     */
    @Field(type = FieldType.Long)
    private Long bodyId;
    /**
     * 类别id
     */
    @Field(type = FieldType.Long)
    private Long categoryId;

    /**
     * 置顶
     */
    @Field(type = FieldType.Integer)
    private Integer weight = Article_Common;


    /**
     * 创建时间
     */
    @Field(type = FieldType.Long)
    private Long createDate;


    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    @TableField(exist = false)
    private String all;

    @Field(type = FieldType.Text,analyzer = "ik_max_word", copyTo = "all")
    @TableField(exist = false)
    private String authorName;
}
