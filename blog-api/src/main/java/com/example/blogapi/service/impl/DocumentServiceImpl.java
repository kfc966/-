package com.example.blogapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.mapper.DocumentMapper;
import com.example.blogapi.dao.pojo.Document;
import com.example.blogapi.service.DocumentService;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.FastdfsUtils;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.DocDownLoadVo;
import com.example.blogapi.vo.DocListVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;
import com.example.blogapi.vo.params.DocUploadParam;
import com.example.blogapi.vo.params.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    public static final String FIFLE_PREFIX = "D:\\demo\\blog\\blog-api\\uploadfile\\";
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private FastdfsUtils fastdfsUtils;

    @Override
    public Result getAllDocuments(PageParams params) {
        Page<Document> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper<>();
        Page selectPage = documentMapper.selectPage(page, wrapper);
        Integer count = documentMapper.selectCount(wrapper);
        DocListVo docListVo = new DocListVo(selectPage.getRecords(), params.getPage(), params.getPageSize(), count);
        List<Long> idList = docListVo.getDocuments().stream().map(Document::getPublisherId).distinct().collect(Collectors.toList());
        List<UserVo> userVoByIds = sysUserService.findUserVoByIds(idList);
        docListVo.getDocuments().stream().forEach(document -> {
            for (UserVo userVo : userVoByIds) {
                if (userVo.getId().equals(String.valueOf(document.getPublisherId()))) {
                    document.setPublisher(userVo.getNickname());
                    return;
                }
            }
        });

        return Result.success(docListVo);
    }

    @Override
    public Result uploadDocument(DocUploadParam uploadParam) {
        MultipartFile multipartFile = uploadParam.getMultipartFile();
        try {
            String storePath = fastdfsUtils.upload(multipartFile);
            Document doc = Document.builder().docUri(storePath).ownerType(uploadParam.getOwnerType())
                    .publisherId(UserThreadLocal.get().getId())
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
}
