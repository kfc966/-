package com.example.blogapi.service;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.Article;
import com.example.blogapi.dao.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import javax.annotation.Resource;
import java.awt.print.Book;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EsArticleService {
    @Resource
    private ArticleRepository articleRepository;

    @Resource
    private ElasticsearchRestTemplate elasticsearchTemplate;

    public void save(Article article){
        articleRepository.save(article);
        log.info("article save success [{}]", JSON.toJSONString(article));

    }

    public  void deleteById(Long id){
        articleRepository.deleteById(id);
        log.info("article delete success [{}]", id);
    }

    public Article findById(Long id){
        Optional<Article> optional = articleRepository.findById(id);
        return  optional.orElse(null);
    }


    public Page<Article> search(String keyword, Integer pageNum, Integer pageSize){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("all", keyword))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
        SearchHits<Article> searchHits = elasticsearchTemplate.search(searchQuery, Article.class);
        List<Article> books = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(books, searchQuery.getPageable(), searchHits.getTotalHits());
    }
}
