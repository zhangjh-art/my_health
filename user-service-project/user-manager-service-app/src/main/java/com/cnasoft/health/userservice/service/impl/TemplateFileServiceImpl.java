package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.io.FileUtil;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.mimetype.MimeTypeUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.enums.TemplateFile;
import com.cnasoft.health.userservice.service.ITemplateFileService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 * @description 文件操作Service
 * @createDate 2022-04-13 11:04:57
 */
@Service
public class TemplateFileServiceImpl implements ITemplateFileService {

    @javax.annotation.Resource
    private FileFeignClient fileFeignClient;

    @Override
    public ResponseEntity<Resource> downloadTemplate(Integer templateType, HttpServletResponse response) {
        TemplateFile templateFile = TemplateFile.getTemplateFile(templateType);
        if (templateFile == null) {
            throw exception("模板类型错误");
        }
        String templateFileName = templateFile.getDescription();

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("template/" + templateFileName);
        if (inputStream == null) {
            throw exception("文件不存在");
        }

        Resource resource = new InputStreamResource(inputStream);
        assert resource.exists();

        String mime = MimeTypeUtil.getMimeTypeBySuffix(FileUtil.getSuffix(templateFileName));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime + ";charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=" + new String(templateFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1))
                .body(resource);

    }

    @Override
    public String persistedFileUrls(List<String> fileTempUrls) {
        if (fileTempUrls == null) {
            return null;
        }
        if (fileTempUrls.size() == 0) {
            return JsonUtils.writeValueAsString(fileTempUrls);
        }
        List<String> persistedFileUrls = Lists.newArrayList();
        for (String fileTempUrl : fileTempUrls) {
            CommonResult<String> feignResult = fileFeignClient.persistence(fileTempUrl);
            feignResult.checkError();
            persistedFileUrls.add(feignResult.getData());
        }
        return JsonUtils.writeValueAsString(persistedFileUrls);
    }
}
