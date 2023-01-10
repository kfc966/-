package com.example.blogapi.service;

import com.example.blogapi.vo.CommentVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.CommentParam;

import java.util.List;

public interface CommentService {
    //根据文章id查找所以评论
    Result commentsByArticleId(Long id);
    //根据父id查找子评论
    List<CommentVo> findCommentsByParentId(Long id);

    Result comment(CommentParam commentParam);
}
