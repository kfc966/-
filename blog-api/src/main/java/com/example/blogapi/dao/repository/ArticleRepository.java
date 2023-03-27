package com.example.blogapi.dao.repository;

import com.example.blogapi.dao.pojo.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface ArticleRepository extends ElasticsearchRepository<Article, Long> {
}