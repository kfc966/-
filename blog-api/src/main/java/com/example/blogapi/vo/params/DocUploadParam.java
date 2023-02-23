package com.example.blogapi.vo.params;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocUploadParam {
    private Byte ownerType;
    private MultipartFile multipartFile;
    private Long docId;

    private String desc;
}
