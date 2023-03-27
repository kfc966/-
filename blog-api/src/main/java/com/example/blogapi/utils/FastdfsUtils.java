package com.example.blogapi.utils;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.SysUser;import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import jdk.nashorn.internal.runtime.options.Option;import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Optional;import java.util.Set;

@Component
@Slf4j
public class FastdfsUtils {
    public static final String DEFAULT_CHARSET = "UTF-8";
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    /**
     * 上传
     * * @param file
     * * @return
     * * @throws IOException
     */
    public String upload(MultipartFile file) throws IOException {
        // 设置文件信息
        Set<MetaData> mataData = new HashSet<>();
        SysUser sysUser = UserThreadLocal.get();
        String account = Optional.ofNullable(sysUser).map(e->String.valueOf(e.getId())).orElse("-");
        mataData.add(new MetaData("account", account));
        mataData.add(new MetaData("description", file.getOriginalFilename()));
        // 上传
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),
				file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), mataData);
        return storePath.getFullPath();
    }

    /**
     * 删除
     * * @param path
     */
    public void delete(String path) {
        fastFileStorageClient.deleteFile(path);
    }

    /**
     * 删除
     * * @param group
     * * @param path
     */
    public void delete(String group, String path) {
        fastFileStorageClient.deleteFile(group, path);
    }

    /**
     * 文件下载
     * * @param path 文件路径，例如：/group1/path=M00/00/00/itstyle.png
     * * @param filename 下载的文件命名
     * * @return
     */
    public byte[]  download(String path) throws IOException {
		// 获取文件
        StorePath storePath = StorePath.parseFromUrl(path);
        byte[] bytes = fastFileStorageClient.downloadFile(storePath.getGroup(), storePath.getPath(), new DownloadByteArray());
        return bytes;
    }
}