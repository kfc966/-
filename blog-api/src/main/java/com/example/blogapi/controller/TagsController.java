package com.example.blogapi.controller;

import com.example.blogapi.service.TagService;
import com.example.blogapi.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;

@RestController
@RequestMapping("tags")
public class TagsController {
    @Autowired
    private TagService tagService;
    @GetMapping("hot")
    public Result hot()
    {
        int limit=6;
        return  tagService.hots(limit);
    }
    @GetMapping
    public Result findAll()
    {
        return  tagService.findAll();
    }
    @GetMapping("detail")
    public Result findAlldetail()
    {
        return  tagService.findAlldetail();
    }
    @GetMapping("detail/{id}")
    public Result findAlldetailById(@PathVariable("id")Long id)

    {
        return  tagService.findAlldetailById(id);
    }
}
