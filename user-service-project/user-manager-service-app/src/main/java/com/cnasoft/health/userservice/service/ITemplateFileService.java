package com.cnasoft.health.userservice.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Administrator
 * @description 文件操作Service
 * @createDate 2022-04-13 11:04:57
 */
public interface ITemplateFileService {

    /**
     * 模板下载
     *
     * @param templateType
     * @param response
     * @return ResponseEntity<Resource>
     * @throws IOException
     */
    ResponseEntity<Resource> downloadTemplate(Integer templateType, HttpServletResponse response) throws IOException;

    /**
     * 持久化oss文件
     *
     * @param fileTempUrls 临时oss文件存储地址
     * @return 持久化后的oss文件存储地址
     */
    String persistedFileUrls(List<String> fileTempUrls);
}
