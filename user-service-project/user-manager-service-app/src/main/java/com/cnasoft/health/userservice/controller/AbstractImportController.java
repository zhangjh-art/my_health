package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Administrator
 */
public abstract class AbstractImportController {

    /**
     * 导入Excel数据
     *
     * @param file  文件
     * @param cover 是否覆盖
     * @return 通用对象
     */
    public abstract CommonResult importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception;
}
