package com.cnasoft.health.userservice.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.cnasoft.health.userservice.excel.MyEasyExcelListener;
import com.cnasoft.health.userservice.excel.service.IExcelLoadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class ExcelLoadServiceImpl<T> implements IExcelLoadService<T> {
    @Override
    public List<T> load(MultipartFile file,Class clazz) throws IOException {
        return EasyExcel.read(file.getInputStream(),
                clazz,
                new MyEasyExcelListener<T>()).sheet().doReadSync();
    }
}
