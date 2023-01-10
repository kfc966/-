package com.example.blogapi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.service.ArticleService;
import com.example.blogapi.service.impl.ArticleServiceImpl;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.PageParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlogApiApplicationTests {

    @Autowired
    private ArticleService articleService;
    @Test
    void contextLoads() {
    }
    @Test
    void demo()
    {
        PageParams params = new PageParams();
        params.setPage(1);
        params.setPageSize(6);
        Result result = articleService.listArticle(params);
        System.out.println(result);
    }
    @Autowired
    private ArticleMapper articleMapper;
    @Test
    void PageTest()
    {
        PageParams params = new PageParams();
        params.setPage(2);
        params.setPageSize(3);
        articleService.listArticle(params);

    }

}
