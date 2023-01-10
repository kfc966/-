package com.example.blogapi.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {
    @Async("taskExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {
        int viewCount=article.getViewCounts();
        LambdaUpdateWrapper<Article> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        //多线程安全
        updateWrapper.eq(Article::getViewCounts,viewCount);
        Article updateArticle=new Article();
        updateArticle.setViewCounts(viewCount+1);
        articleMapper.update(updateArticle,updateWrapper);

        try {
            Thread.sleep(5000);
            System.out.println("更新完成了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
