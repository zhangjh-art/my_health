package com.cnasoft.health.userservice.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.cnasoft.health.common.util.io.FileUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.excel.service.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * liukl 文件存储，可以把实现改到天翼云
 */
@Slf4j
@Component
public class FileServiceImpl<T> implements IFileService<T> {
    @Override
    public String saveFile(FileFeignClient fileFeignClient, String fileName, List<T> list, Class head) {
        String tmpDir = System.getProperty("java.io.tmpdir");

        String path = String.format("%s%s", tmpDir, fileName);
        EasyExcel.write(path, head).excelType(ExcelTypeEnum.XLS)
                .sheet("错误数据").doWrite(list);

        // 将文件上传到天翼云
        File file = new File(path);
        if (file.exists()) {
            MultipartFile multipartFile = FileUtil.getMultipartFile(file);

            CommonResult<List<String>> uploadResult = fileFeignClient.uploadFileToOss(new MultipartFile[]{multipartFile}, "true");
            uploadResult.checkError();

            // 删除临时文件
            file.delete();

            return uploadResult.getData().get(0);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
