package com.cnasoft.health.userservice.controller;


import com.cnasoft.health.userservice.service.ITemplateFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 文件管理
 *
 * @author zcb
 * @date 2022/4/14
 */
@Slf4j
@RestController
@RequestMapping("/templateFile")
@Api(tags = "模板文件下载API")
public class TemplateFileController {

    @Resource
    private ITemplateFileService fileService;

    /**
     * 下载模板文件
     *
     * @param templateType 模板类型，1：学生；2：家长；3：校教职工；4：校心理老师；5：区域职员；6：区域心理教研员；7：班级；8：量表导入模板
     */
    @ApiImplicitParams({
            @ApiImplicitParam(value = "1：学生；2：家长；3：校教职工；4：校心理老师；5：区域职员；6：区域心理教研员；7：班级；8：量表导入模板")
    })
    @ApiOperation(value = "模板文件下载接口")
    @GetMapping(value = "download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadTemplate(@RequestParam Integer templateType, HttpServletResponse response) throws IOException {
        return fileService.downloadTemplate(templateType, response);
    }
}
