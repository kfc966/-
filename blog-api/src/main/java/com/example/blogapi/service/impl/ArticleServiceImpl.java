package com.example.blogapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.mapper.ArticleTagMapper;
import com.example.blogapi.dao.mapper.CategoryMapper;
import com.example.blogapi.dao.pojo.*;
import com.example.blogapi.dos.Archives;
import com.example.blogapi.dao.mapper.ArticleBodyMapper;
import com.example.blogapi.service.*;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.*;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Override
    public Result listArticle(PageParams pageParams) {
       Page<Article> articlePage=new Page<>(pageParams.getPage(),pageParams.getPageSize());//当前页，和每页大小
        IPage<Article> articleIPage=this.articleMapper.listArticles(articlePage,pageParams.getCategoryId(),pageParams.getTagId(),
                pageParams.getYear(),pageParams.getMonth());
        List<Article> articleList = articleIPage.getRecords();
        return  Result.success(copyList(articleList,true,true,false,false));
    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by viewcount desc limit 5
        List<Article> articleList = articleMapper.selectList(queryWrapper);
        return  Result.success(copyList(articleList,false,false,false,false));
    }

    @Override
    public Result newArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+5);
        List<Article> articleList = articleMapper.selectList(queryWrapper);
        return  Result.success(copyList(articleList,false,false,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archives=articleMapper.listArchives();
        return Result.success(archives);
    }

    @Override
    public Result searchArticle(PageParams params) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().
                withQuery(QueryBuilders.matchQuery("all", params.getSearchKey()))
                .withPageable(PageRequest.of(params.getPage() - 1, params.getPageSize())).build();
        SearchHits<Article> searchHits = elasticsearchTemplate.search(query, Article.class);
        List<Article> articles = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return Result.success(articles);
    }

    //根据id查找文章的内容
    @Autowired
    private ThreadService threadService;
    @Override
    public Result findArticleById(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true, true, true);
        threadService.updateArticleViewCount(articleMapper,article);
        return  Result.success(articleVo);
    }
    //发布文章
    @Override
    public Result publish(ArticleParam articleParam) {
        SysUser sysUser = UserThreadLocal.get();
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setCreateDate(System.currentTimeMillis());
        article.setSummary(articleParam.getSummary());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        article.setCommentCounts(0);
        //插入后,mybatis-plus用set方法回写对象的主键
        this.articleMapper.insert(article);
        //tag
        List<TagVo> tagVoList = articleParam.getTags();
        if(tagVoList!=null)
        {
            for (TagVo tagVo: tagVoList)
            {
                Long id = article.getId();
                ArticleTag articleTag=new ArticleTag();
                System.out.println(tagVo.getId());
                articleTag.setTagId(Long.parseLong(tagVo.getId()));
                articleTag.setArticleId(id);
                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);
        //System.out.println(articleBody.getId()+"---------------");
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String, String> map=new HashMap<>();
        map.put("id",article.getId().toString());
        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory)
    {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for(Article article:records)
        {
            articleVoList.add(copy(article,isTag,isAuthor,isBody,isCategory));
        }
        return  articleVoList;
    }

    private ArticleVo copy(Article article,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory)
    {
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article,articleVo);
        articleVo.setId(String.valueOf(article.getId()));
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-mm-dd hh:mm"));
        if(isTag)
        {

            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor)
        {
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if(isBody)
        {
            Long bodyId=article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if(isCategory)
        {
            CategoryVo categoryVo = categoryService.findCategoryById(article.getCategoryId());
            articleVo.setCategory(categoryVo);
        }
        return  articleVo;
    }


    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return  articleBodyVo;
    }
}
