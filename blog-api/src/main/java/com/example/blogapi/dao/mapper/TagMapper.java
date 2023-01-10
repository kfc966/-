package com.example.blogapi.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogapi.dao.pojo.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TagMapper extends BaseMapper<Tag> {
    
    List<Long> findHotsTagIds(int limit);

    /***
     * 根据文章id查询标签列表
     */
    List<Tag> findTagsByArticleId(Long articleId);

    List<Tag> findTagsByTagId(List<Long> tagIds);
}
