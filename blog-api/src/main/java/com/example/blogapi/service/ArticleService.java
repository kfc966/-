package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleService {
    //分页查询
    Result listArticle(PageParams pageParams);

    Result hotArticle(int limit);
    /**
     * 最新文章
     */

    Result newArticle(int limit);

    Result findArticleById(Long articleId);

    Result publish(ArticleParam articleParam);

    Result listArchives();
}
