package com.example.blogapi;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.mapper.SysUserMapper;
import com.example.blogapi.dao.pojo.Article;
import com.example.blogapi.dao.repository.ArticleRepository;
import com.example.blogapi.service.ArticleService;
import com.example.blogapi.service.EsArticleService;
import com.example.blogapi.service.impl.ArticleServiceImpl;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.PageParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;

@SpringBootTest(classes = {BlogApiApplication.class})
class BlogApiApplicationTests {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private EsArticleService esArticleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void demo() {
        PageParams params = new PageParams();
        params.setPage(1);
        params.setPageSize(6);
        Result result = articleService.listArticle(params);
        System.out.println(result);
    }

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    void PageTest() {
        PageParams params = new PageParams();
        params.setPage(2);
        params.setPageSize(3);
        articleService.listArticle(params);

    }

    @Test
    public void saveArticleToEs() {
        List<Article> articles = articleMapper.selectList(null);
        for (Article article : articles) {
            article.setAuthorName(sysUserMapper.selectById(article.getAuthorId()).getNickname());
        }
        articleRepository.saveAll(articles);
    }

    @Test
    public void testEsFind(){
        Page<Article> articles = esArticleService.search("spring", 1, 20);
        System.out.println(JSON.toJSONString(articles));
//        for (Article article : articleRepository.findAll()) {
//            System.out.println(article);
//        }

    }
}
