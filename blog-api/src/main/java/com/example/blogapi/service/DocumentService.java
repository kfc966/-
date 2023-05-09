package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ApplyDocParam;
import com.example.blogapi.vo.params.DocUploadParam;
import com.example.blogapi.vo.params.PageParams;

import java.util.Map;

public interface DocumentService {

    public Result getAllDocuments(PageParams params);
    public Result uploadDocument(DocUploadParam uploadParam);

    Result downloadDocument(Long documentId);

    public Result updateDocument(DocUploadParam uploadParam);

    public Result deleteDocument(Long documentId);


    Result searchDocument(PageParams params);

    Result applyDocument(ApplyDocParam applyDocParam);

    Result getApplyInfo();

    Result acceptApply(Map<?,?>data);
}
