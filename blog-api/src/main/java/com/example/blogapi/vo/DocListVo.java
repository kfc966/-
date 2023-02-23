package com.example.blogapi.vo;

import com.example.blogapi.dao.pojo.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DocListVo {
    private List<Document> documents;
    private InnerPage innerPage;


    public DocListVo(List<Document> documents, int pageSize,int pageNumber,int total) {
        this.documents = documents;
        InnerPage page = new InnerPage(pageSize, pageNumber, total);
        this.innerPage = page;
    }

    @Data
    @AllArgsConstructor
    public static class InnerPage {
        int pageSize;
        int pageNumber;
        int total;
    }
}
