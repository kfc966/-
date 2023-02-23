package com.example.blogapi.controller;

import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.DocumentService;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.DocDownLoadVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.DocUploadParam;
import com.example.blogapi.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;

@Controller
@RequestMapping("/document")
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
        Long id = Long.parseLong("1404448463944462338");
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        UserThreadLocal.put(sysUser);
        return documentService.uploadDocument(uploadParam);
    }
    @GetMapping ("/download/{id}")
    public  ResponseEntity<byte[]> download(@PathVariable("id") Long documentId) throws IOException {
        Long id = Long.parseLong("1404448463944462338");
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        UserThreadLocal.put(sysUser);
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
        Long id = Long.parseLong("1404448463944462338");
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        UserThreadLocal.put(sysUser);
        return  documentService.updateDocument(uploadParam);
    }
    @PostMapping("/delete")
    @ResponseBody
    public Result delete(@RequestParam("documentId") Long documentId){
        Long id = Long.parseLong("1404448463944462338");
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        UserThreadLocal.put(sysUser);
        return documentService.deleteDocument(documentId);
    }

}
