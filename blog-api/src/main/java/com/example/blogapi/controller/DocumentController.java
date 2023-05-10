package com.example.blogapi.controller;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.DocumentService;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.DocDownLoadVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ApplyDocParam;
import com.example.blogapi.vo.params.DocUploadParam;
import com.example.blogapi.vo.params.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@Controller
@RequestMapping("/document")
@Slf4j
public class DocumentController {
    @Autowired
    private DocumentService documentService;



    @PostMapping("/all")
    @ResponseBody
    public Result getAllDocuments(@RequestBody  PageParams params){
        return documentService.getAllDocuments(params);
    }

    @PostMapping("/upload")
    @ResponseBody
    public Result upload(DocUploadParam uploadParam){
        return documentService.uploadDocument(uploadParam);
    }
    @GetMapping ("/download/{id}")
    public  ResponseEntity<byte[]> download(@PathVariable("id") Long documentId) throws IOException {
        Result result = documentService.downloadDocument(documentId);
        DocDownLoadVo data = (DocDownLoadVo) result.getData();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition",
                "attachment;filename=" +  URLEncoder.encode(data.getDocTitle(),"utf-8"));
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data.getContent(), headers, HttpStatus.OK);
        return  responseEntity;
    }
    @PostMapping("/update")
    @ResponseBody
    public Result update(DocUploadParam uploadParam){
        return  documentService.updateDocument(uploadParam);
    }
    @PostMapping("/delete")
    @ResponseBody
    public Result delete(@RequestParam("documentId") Long documentId){
        return documentService.deleteDocument(documentId);
    }

    @PostMapping("/search")
    @ResponseBody
    public Result searchDocument(@RequestBody PageParams params){
        log.info("searchDocument request params:[{}] ", JSON.toJSONString(params));
        return  documentService.searchDocument(params);
    }

    /**
     * 申请文档权限
     */
    @PostMapping("/apply")
    @ResponseBody
    public Result applyDocument(@RequestBody ApplyDocParam applyDocParam) {
        log.info("applyDocument request params:[{}] ", JSON.toJSONString(applyDocParam));
        return  documentService.applyDocument(applyDocParam);
    }

    /**
     * 查询申请信息
     * @return
     */
    @GetMapping("/apply")
    @ResponseBody
    public Result applyDocument() {
        return  documentService.getApplyInfo();
    }

    /**
     * 处理审批
     */
    @PostMapping("/accept")
    @ResponseBody
    public Result accept(@RequestBody Map<?,?>data){
        return  documentService.acceptApply(data);
    }



}
