package com.cnasoft.health.userservice.excel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IExcelLoadService<T> {
    List<T> load(MultipartFile file,Class clazz) throws IOException;
}
