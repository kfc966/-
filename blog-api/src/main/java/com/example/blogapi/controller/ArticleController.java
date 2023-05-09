package com.example.blogapi.controller;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.common.aop.LogAnnotation;
import com.example.blogapi.common.aop.cache.Cache;
import com.example.blogapi.service.ArticleService;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//json数据交互
@RestController
@RequestMapping("articles")
@Slf4j
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @PostMapping
    public Result listArticle(@RequestBody PageParams pageParams){
        return articleService.listArticle(pageParams);
    }
    //首页热门文章
    @PostMapping("hot")
    @Cache(expire = 3*60*1000,name ="hotArticle" )
    public Result hotArticle(){
        int limit=5;
        return articleService.hotArticle(limit);
    }
    @PostMapping("new")
    public Result newArticle(){
        int limit=5;
        return articleService.newArticle(limit);
    }

    @PostMapping("listArchives")
    @LogAnnotation(module = "文章",operator= "获取文章列表")
    public Result listArchives(){

        return articleService.listArchives();
    }
    @PostMapping("/view/{id}")
    public  Result findArticleById(@PathVariable("id") Long articleId)
    {
        return  articleService.findArticleById(articleId);
    }
//    接口url：/articles/publish
//    请求方式：POST
    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam)
    {
        return  articleService.publish(articleParam);
    }

    @PostMapping("/search")
    @ResponseBody
    public Result searchArticle(@RequestBody PageParams params){
        log.info("searchArticle request params:[{}] ", JSON.toJSONString(params.getSearch().getAll()));
        return  articleService.searchArticle(params);
    }
}
