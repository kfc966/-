package com.example.blogapi.controller;

import com.example.blogapi.service.CategoryService;
import com.example.blogapi.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("categorys")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping
    public Result listCategory()
    {
        return  categoryService.findAll();
    }
    @GetMapping("detail")
    public  Result categoryDetail()
    {
        return categoryService.findAlldetail();
    }
    @GetMapping("detail/{id}")
    public  Result categoryDetailById(@PathVariable("id")Long id)
    {
        return categoryService.findAlldetailById(id);
    }
}
