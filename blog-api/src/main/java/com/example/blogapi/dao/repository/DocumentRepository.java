package com.example.blogapi.dao.repository;

import com.example.blogapi.dao.pojo.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DocumentRepository extends ElasticsearchRepository<Document,String> {
}
