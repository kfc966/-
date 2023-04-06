package com.example.blogapi;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.mapper.DocumentMapper;
import com.example.blogapi.dao.mapper.SysUserMapper;
import com.example.blogapi.dao.pojo.Article;
import com.example.blogapi.dao.pojo.Document;
import com.example.blogapi.dao.repository.ArticleRepository;
import com.example.blogapi.dao.repository.DocumentRepository;
import com.example.blogapi.service.ArticleService;
import com.example.blogapi.service.EsArticleService;
import com.example.blogapi.service.impl.ArticleServiceImpl;
import com.example.blogapi.utils.FastdfsUtils;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.PageParams;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;


import java.util.List;

@SpringBootTest(classes = {BlogApiApplication.class})
@Slf4j
class BlogApiApplicationTests {

    @Autowired
    private ArticleService articleService;

    @Resource
    private DocumentMapper documentMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private EsArticleService esArticleService;

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FastdfsUtils fastdfsUtils;

    @Test
    void contextLoads() {
    }

    @Test
    void demo() {
        PageParams params = new PageParams();
        params.setPage(1);
        params.setPageSize(6);
        Result result = articleService.listArticle(params);
        System.out.println(result);
    }

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    void PageTest() {
        PageParams params = new PageParams();
        params.setPage(2);
        params.setPageSize(3);
        articleService.listArticle(params);

    }


    @Test
    public   void TestFastDfsUpload() throws IOException {
        File file = new File("uploadfile/50da7e17-c73f-41f2-a21f-83e634ab201dQQ截图20220815215222.png");
        InputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(),file.getName(),"text",inputStream);
        String storePath = fastdfsUtils.upload(multipartFile);
        log.info("storePath:[{}]",storePath);
        //StorePath [group=group1, path=M00/00/00/CgAQAmQhQDCAUqyfAACde9yfmf4145.png]
    }

    @Test
    public  void TestDownload() throws IOException {
        String path = "group1/M00/00/00/CgAQAmQhQgaAIJ-EAACde9yfmf4793.png";
        byte[] download = fastdfsUtils.download(path);
        FileOutputStream fileOutputStream = new FileOutputStream("uploadfile/kk.png");
        fileOutputStream.write(download);
        fileOutputStream.close();
    }

    @Test
    public void saveArticleToEs() {
        List<Article> articles = articleMapper.selectList(null);
        for (Article article : articles) {
            article.setAuthorName(sysUserMapper.selectById(article.getAuthorId()).getNickname());
        }
        articleRepository.saveAll(articles);
    }

    @Test
    public void saveDocToEs(){
        List<Document> documents = documentMapper.selectList(null);
        for (Document document : documents) {
            document.setPublisher(sysUserMapper.selectById(document.getPublisherId()).getNickname());
        }
        documentRepository.saveAll(documents);
    }

    @Test
    public void testEsFind(){
        Page<Article> articles = esArticleService.search("spring", 1, 20);
        System.out.println(JSON.toJSONString(articles));
//        for (Article article : articleRepository.findAll()) {
//            System.out.println(article);
//        }

    }


    @Test
    public void jiexiword() throws IOException {
        File file = new File("D:\\tmp\\testsearch.docx");
        XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
        log.info("doc对象{}",doc.toString());
        // 使用 XWPFWordExtractor 提取 Word 文档内容
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        System.out.println(text);
    }
}
