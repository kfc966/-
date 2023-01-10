package com.example.blogapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogapi.dao.mapper.TagMapper;
import com.example.blogapi.dao.pojo.Tag;
import com.example.blogapi.service.TagService;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.TagVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;

    public TagVo copy(Tag tag){
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag,tagVo);
        tagVo.setId(tag.getId().toString());
        return tagVo;
    }
    public List<TagVo> copyList(List<Tag> tagList){
        List<TagVo> tagVoList = new ArrayList<>();
        for (Tag tag : tagList) {
            tagVoList.add(copy(tag));
        }
        return tagVoList;
    }
    @Override
    public List<TagVo> findTagsByArticleId(Long articleId) {
        //mybatispul无法多表查询
        List<Tag> tags=tagMapper.findTagsByArticleId(articleId);
        return copyList(tags);
    }

    @Override
    public Result hots(int limit) {
        List<Long> tagIds= tagMapper.findHotsTagIds(limit);
        if(CollectionUtils.isEmpty(tagIds))
        {
            return  Result.success(Collections.emptyList());
        }
        List<Tag> tagList=tagMapper.findTagsByTagId(tagIds);
        return Result.success(tagList);
    }

    @Override
    public Result findAll() {
        LambdaQueryWrapper<Tag> tagQueryWrapper=new LambdaQueryWrapper<>();
        tagQueryWrapper.select(Tag::getId,Tag::getTagName);
        List<Tag> tagList = tagMapper.selectList(tagQueryWrapper);
        return  Result.success(copyList(tagList));
    }

    @Override
    public Result findAlldetail() {
        LambdaQueryWrapper<Tag> tagQueryWrapper=new LambdaQueryWrapper<>();
        List<Tag> tagList = tagMapper.selectList(tagQueryWrapper);
        return  Result.success(copyList(tagList));
    }

    @Override
    public Result findAlldetailById(Long id) {
        Tag tag = tagMapper.selectById(id);
        return  Result.success(copy(tag));
    }
}
