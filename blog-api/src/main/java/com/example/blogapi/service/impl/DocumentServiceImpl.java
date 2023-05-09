package com.example.blogapi.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.mapper.DocumentMapper;
import com.example.blogapi.dao.mapper.DocumentOwerMapper;
import com.example.blogapi.dao.pojo.Document;
import com.example.blogapi.dao.pojo.DocumentOwer;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.DocumentService;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.FastdfsUtils;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.DocDownLoadVo;
import com.example.blogapi.vo.DocListVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;
import com.example.blogapi.vo.params.ApplyDocParam;
import com.example.blogapi.vo.params.DocUploadParam;
import com.example.blogapi.vo.params.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    public static final String PUBLISHER_PREFIX = "PUBLISHER_LIST_";
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private FastdfsUtils fastdfsUtils;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Resource
    private DocumentOwerMapper documentOwerMapper;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolExecutor;

    @Override
    public Result getAllDocuments(PageParams params) {
        Page<Document> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper<>();
        Page selectPage = documentMapper.selectPage(page, wrapper);
        Integer count = documentMapper.selectCount(wrapper);
        DocListVo docListVo = new DocListVo(selectPage.getRecords(), params.getPageSize(), params.getPage(), count);
        List<Long> idList = docListVo.getDocuments().stream().map(Document::getPublisherId).distinct().collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(idList)){
            List<UserVo> userVoByIds = sysUserService.findUserVoByIds(idList);
            docListVo.getDocuments().stream().forEach(document -> {
                for (UserVo userVo : userVoByIds) {
                    if (userVo.getId().equals(String.valueOf(document.getPublisherId()))) {
                        document.setPublisher(userVo.getNickname());
                        document.setAll(document.getContent());
                        return;
                    }
                }
            });}
        // 设置可见性
        setViewAble(docListVo.getDocuments());
        return Result.success(docListVo);
    }

    private void setViewAble(List<Document> documents) {
        SysUser sysUser = UserThreadLocal.get();
        documents.forEach(doc -> {
            if (doc.getOwnerType() == 0) {
                doc.setViewable(Boolean.TRUE);
            } else if (Long.compare(sysUser.getId(), doc.getPublisherId()) == 0) {
                doc.setViewable(Boolean.TRUE);
            }else {
                doc.setViewable(Boolean.FALSE);
            }
        });
    }

    @Override
    public Result uploadDocument(DocUploadParam uploadParam) {
        MultipartFile multipartFile = uploadParam.getMultipartFile();
        try {
            String storePath = fastdfsUtils.upload(multipartFile);
            Document doc = Document.builder().docUri(storePath).ownerType(uploadParam.getOwnerType())
                    .publisherId(UserThreadLocal.get().getId())
                    .docDesc(uploadParam.getDesc())
                    .updateTime(System.currentTimeMillis()).docTitle(multipartFile.getOriginalFilename()).build();
            int insert = documentMapper.insert(doc);
            return Result.success(insert);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Result downloadDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return Result.fail(20001, "Document not found");
        }
        if (document.getOwnerType() == 1 &&
                !document.getPublisherId().equals(UserThreadLocal.get().getId())) {
            return Result.fail(20001, "Document not found");
        }
        byte[] download = null;
        try {
            download = fastdfsUtils.download(document.getDocUri());
        } catch (IOException e) {
            log.error("Failed to download document documentId[[]]", documentId);
            throw new RuntimeException(e);
        }
        DocDownLoadVo docDownLoadVo = new DocDownLoadVo();
        docDownLoadVo.setDocTitle(document.getDocTitle());
        docDownLoadVo.setContent(download);
        return Result.success(docDownLoadVo);
    }

    @Override
    public Result updateDocument(DocUploadParam uploadParam) {
        Document oldDoc = documentMapper.selectById(uploadParam.getDocId());
        if (Objects.isNull(oldDoc) || oldDoc.getOwnerType() == 1 && !oldDoc.getPublisherId().equals(UserThreadLocal.get().getId())) {
            return Result.fail(20001, "权限错误");
        }
        MultipartFile multipartFile = uploadParam.getMultipartFile();
        // 只是单纯修改文件的描述和权限
        Document.DocumentBuilder documentBuilder = Document.builder().id(String.valueOf(uploadParam.getDocId()))
                .ownerType(uploadParam.getOwnerType())
                .docDesc(uploadParam.getDesc())
                .publisherId(UserThreadLocal.get().getId())
                .updateTime(System.currentTimeMillis()).docTitle(multipartFile.getOriginalFilename());

        try {
            if (multipartFile != null) {
                // todo 删除原来的文件
                String storePath = fastdfsUtils.upload(multipartFile);
                documentBuilder.docUri(storePath);
            }
            int insert = documentMapper.updateById(documentBuilder.build());
            return Result.success(insert);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Result deleteDocument(Long documentId) {
        Document oldDoc = documentMapper.selectById(documentId);
        if (Objects.isNull(oldDoc) || oldDoc.getOwnerType() == 1 && !oldDoc.getPublisherId().equals(UserThreadLocal.get().getId())) {
            return Result.fail(20001, "权限错误");
        }
        int i = documentMapper.deleteById(documentId);
//        File oldDocFile = new File(FIFLE_PREFIX + oldDoc.getDocUri());
//        oldDocFile.delete();
        return Result.success(i);
    }

    @Override
    public Result searchDocument(PageParams params) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        PageParams.SearchParams searchParams = params.getSearch();
        //添加作者条件
        if (StringUtils.isNoneBlank(searchParams.getPublisher())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("publisher", searchParams.getPublisher()));
        }
        //添加起止时间
        if (searchParams.getBeginTime() != null && searchParams.getEndTime() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("updateTime")
                    .gt(searchParams.getBeginTime())
                    .lte(searchParams.getEndTime()));
        }
        //添加所有权
        if (searchParams.getOwerType() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("ownerType", searchParams.getOwerType()));
        }
        //添加关键词
        if (StringUtils.isNoneBlank(searchParams.getAll())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchParams.getAll()));
        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }

//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().
//                withQuery(QueryBuilders.matchQuery("all", params.getSearchKey()))
//                .withPageable(PageRequest.of(params.getPage() - 1, params.getPageSize()));
//        if(StringUtils.isBlank(params.getSearchKey())){
//            queryBuilder.withQuery(QueryBuilders.matchAllQuery());
//        }
        queryBuilder.withQuery(boolQueryBuilder).
                withPageable(PageRequest.of(params.getPage() - 1, params.getPageSize()));
        // 对all查询进行高亮
        queryBuilder.withHighlightFields(new HighlightBuilder.Field("all"));
        SearchHits<Document> searchHits = elasticsearchTemplate.search(queryBuilder.build(), Document.class);
        List<Document> documents = searchHits.stream().map(s -> {
            Document document = s.getContent();
            List<String> highlightField = s.getHighlightField("all");
            if (!CollectionUtils.isEmpty(highlightField)) {
                document.setAll(highlightField.get(0));
            }
            if (StringUtils.isBlank(document.getAll())) {
                document.setAll(document.getContent());
            }
            if (StringUtils.length(document.getAll()) > 100) {
                document.setAll(document.getAll().substring(0, 100));
            }
            return document;
        }).collect(Collectors.toList());
        DocListVo docListVo = new DocListVo(documents, params.getPageSize(), params.getPage(), (int) searchHits.getTotalHits());

        //设置可见性
        setViewAble(docListVo.getDocuments());
        return Result.success(docListVo);
    }

    @Override
    public Result applyDocument(ApplyDocParam applyDocParam) {
        // todo 存入mysql数据库
        Document document = documentMapper.selectById(applyDocParam.getDocId());
        if (Objects.isNull(document)) {
            return Result.fail(20001, "文档不存在");
        }
        applyDocParam.setDocTitle(document.getDocTitle());
        applyDocParam.setDocDesc(document.getDocDesc());
        applyDocParam.setPublisherId(document.getPublisherId());
        applyDocParam.setApplyUserId(UserThreadLocal.get().getId());
        applyDocParam.setUpdateTime(System.currentTimeMillis());
        applyDocParam.setFinished(Boolean.FALSE);
        redisTemplate.opsForList().rightPush(PUBLISHER_PREFIX + document.getPublisherId(), JSON.toJSONString(applyDocParam));
        return Result.success("提交成功");
    }


    @Override
    public Result getApplyInfo() {
        String key = PUBLISHER_PREFIX + UserThreadLocal.get().getId();
        List<String> stringList = redisTemplate.opsForList().range(key, 0, -1);
        List<ApplyDocParam> receive = new ArrayList<>();
        Long id = UserThreadLocal.get().getId();
        for (String s : stringList) {
            ApplyDocParam applyDocParam = JSON.parseObject(s, ApplyDocParam.class);
            receive.add(applyDocParam);
        }
        // todo 删除过期的消息
        Map<String,Object> map = new HashMap<>();
        map.put("receive",receive);
        return Result.success(map);
    }

    @Override
    public Result acceptApply(Map<?,?>data) {
        Long docId = (Long)data.get("docId");
        Long applyUserId = (Long)data.get("applyUserId");
        DocumentOwer documentOwer = documentOwerMapper.selectById(docId);
        if(documentOwer == null){
            DocumentOwer ower = new DocumentOwer();
            ower.setId(docId);
            ower.setUpdateTime(System.currentTimeMillis());
            List<Long> list = Arrays.asList(applyUserId, UserThreadLocal.get().getId());
            ower.setOwer(JSON.toJSONString(list));
            documentOwerMapper.insert(ower);
        }
        else {
            String ower = documentOwer.getOwer();
            List<Long> owerList = JSON.parseObject(ower, new TypeReference<List<Long>>() {
            });
            owerList.add(applyUserId);
            documentOwer.setOwer(JSON.toJSONString(owerList));
            documentOwer.setUpdateTime(System.currentTimeMillis());
            documentOwerMapper.updateById(documentOwer);
        }
        // 更新redis
        String key = PUBLISHER_PREFIX + UserThreadLocal.get().getId();
        threadPoolExecutor.execute(()->{
            List<String> list = redisTemplate.opsForList().range(key, 0, -1);
            for (int i=0;i<list.size();i++) {
                ApplyDocParam applyDocParam = JSON.parseObject(list.get(i), ApplyDocParam.class);
                if (docId.equals(applyDocParam.getDocId()) && applyUserId.equals(applyDocParam.getApplyUserId())) {
                    applyDocParam.setFinished(Boolean.TRUE);
                    applyDocParam.setUpdateTime(System.currentTimeMillis());
                    redisTemplate.opsForList().set(key, i, JSON.toJSONString(applyDocParam));
                    break;
                }
            }
        });
        return Result.success(docId);
    }
}
