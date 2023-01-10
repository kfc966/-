package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.TagVo;

import java.util.List;

public interface TagService {
    List<TagVo> findTagsByArticleId(Long articleId);

    Result hots(int limit);

    Result findAll();

    Result findAlldetail();

    Result findAlldetailById(Long id);
}
