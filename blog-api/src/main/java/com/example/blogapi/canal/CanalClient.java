package com.example.blogapi.canal;

import cn.hutool.Hutool;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.example.blogapi.dao.pojo.Document;
import com.example.blogapi.dao.repository.DocumentRepository;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.FastdfsUtils;
import com.example.blogapi.vo.UserVo;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CanalClient implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private DocumentRepository documentRepository;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private FastdfsUtils fastdfsUtils;

    private final String IPAddress = "35.229.244.93";

    /**
     * 实时数据同步程序
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException, IOException {
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(
                IPAddress, 11111), "test", "", "");
        while (true) {
            //连接
            connector.connect();
            //订阅数据库
            connector.subscribe("blog.ms_document");
            //获取数据
            Message message = connector.get(10);
            List<CanalEntry.Entry> entryList = message.getEntries();
            log.info("------canal拉取数据------");
            if (CollectionUtils.isEmpty(entryList)) {
                //没有数据，休息一会
                TimeUnit.SECONDS.sleep(5);
            } else {
                for (CanalEntry.Entry entry : entryList) {
                    //获取类型
                    CanalEntry.EntryType entryType = entry.getEntryType();

                    //判断类型是否为ROWDATA
                    if (CanalEntry.EntryType.ROWDATA.equals(entryType)) {
                        //获取序列化后的数据
                        ByteString storeValue = entry.getStoreValue();
                        //反序列化数据
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                        //获取当前事件操作类型
                        CanalEntry.EventType eventType = rowChange.getEventType();
                        //获取数据集
                        List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();

                        if (eventType == CanalEntry.EventType.INSERT) {
                            log.info("------新增操作------");

                            List<Document> documentList = new ArrayList<>();
                            for (CanalEntry.RowData rowData : rowDataList) {
                                documentList.add(createDocument(rowData.getAfterColumnsList()));
                            }
                            findAuthorName(documentList);
                            //es批量新增文档
                            documentRepository.saveAll(documentList);
                            //打印新增集合
                            log.info("insert to es [{}]",JSON.toJSONString(documentList));
                        } else if (eventType == CanalEntry.EventType.UPDATE) {
                            log.info("------更新操作------");

                            List<Document> beforeMusicList = new ArrayList<>();
                            List<Document> afterMusicList = new ArrayList<>();
                            for (CanalEntry.RowData rowData : rowDataList) {
                                //更新前
                                beforeMusicList.add(createDocument(rowData.getBeforeColumnsList()));
                                //更新后
                                afterMusicList.add(createDocument(rowData.getAfterColumnsList()));
                            }
                            //es批量更新文档
                            documentRepository.saveAll(afterMusicList);
                            findAuthorName(afterMusicList);
                            //打印更新前集合
                            log.info("更新前：{}", JSON.toJSONString(beforeMusicList));
                            //打印更新后集合
                            log.info("更新后：{}", JSON.toJSONString(afterMusicList));
                        } else if (eventType == CanalEntry.EventType.DELETE) {
                            //删除操作
                            log.info("------删除操作------");

                            List<Document> documentList = new ArrayList<>();
                            for (CanalEntry.RowData rowData : rowDataList) {
                                documentList.add(createDocument(rowData.getBeforeColumnsList()));
                            }
                            //es批量删除文档
                            documentRepository.deleteAll(documentList);
                            //打印删除id集合
                            log.info("canal 删除 es数据[{}] ",JSON.toJSONString(documentList));
                        }
                    }
                }
            }
        }
    }


    @Retryable(value = {RuntimeException.class} ,maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public Document createDocument(List<CanalEntry.Column> columnList){
        JSONObject jsonObject = new JSONObject();
        for (CanalEntry.Column column : columnList) {
            jsonObject.fluentPut(StrUtil.toCamelCase(column.getName()), column.getValue());
        }
        Document document = jsonObject.toJavaObject(Document.class);
        String extension = FilenameUtils.getExtension(document.getDocTitle());
        switch (extension) {
            case "docx":
                setDocumentContent(document);
                break;
            case "doc":
                setDocumentContent(document);
                break;
            case "txt":
                setTxtContent(document);
            default:
                break;
        }
        return document;
    }

    private void setDocumentContent(Document document) {

        try {
            byte[] bytes = fastdfsUtils.download(document.getDocUri());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            // 将字节数组转换为 XWPFDocument 对象4
            log.info("下载的bytes-{}",inputStream.toString());
            XWPFDocument doc = new XWPFDocument(inputStream);
            log.info("doc对象{}",doc.toString());
            // 使用 XWPFWordExtractor 提取 Word 文档内容
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            document.setContent(text.substring(0,300));

            // 关闭 XWPFDocument 和 XWPFWordExtractor
            doc.close();
            extractor.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //todo 重构成责任链模式
    private void setTxtContent(Document document) {
        try {
            byte[] bytes = fastdfsUtils.download(document.getDocUri());
            String text = new String(bytes, StandardCharsets.UTF_8);
            document.setContent(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void findAuthorName(List<Document> documentList){
        List<Long> idList = documentList.stream().map(Document::getPublisherId).distinct().collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(idList)){
            List<UserVo> userVoByIds = sysUserService.findUserVoByIds(idList);
            documentList.stream().forEach(document -> {
                for (UserVo userVo : userVoByIds) {
                    if (userVo.getId().equals(String.valueOf(document.getPublisherId()))) {
                        document.setPublisher(userVo.getNickname());
                        return;
                    }
                }
            });}
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        CompletableFuture.runAsync(()-> {
            try {
                this.run();
            } catch (InterruptedException | IOException e) {
                log.error("canal exception!!!");
            }
        });
    }
}