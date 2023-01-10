package com.example.blogapi.service;

import com.example.blogapi.vo.CategoryVo;
import com.example.blogapi.vo.Result;

public interface CategoryService {

    CategoryVo findCategoryById(Long id);

    Result findAll();

    Result findAlldetail();

    Result findAlldetailById(Long id);
}