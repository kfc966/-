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
import com.example.blogapi.vo.UserVo;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
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

    private final String IPAddress = "13.212.18.162";

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

    /**
     * 根据canal获取的数据创建Music对象
     *
     * @param columnList
     * @return
     */
    private Document createDocument(List<CanalEntry.Column> columnList) {
        JSONObject jsonObject = new JSONObject();
        for (CanalEntry.Column column : columnList) {
            jsonObject.fluentPut(StrUtil.toCamelCase(column.getName()), column.getValue());
        }
        Document document = jsonObject.toJavaObject(Document.class);
        return document;
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