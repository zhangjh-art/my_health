package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.feign.dto.IntelligentImportVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IIntelligentImportService;
import com.cnasoft.health.userservice.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.cnasoft.health.common.vo.CommonResult.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * 智能导入
 *
 * @author ganghe
 * @date 2022/5/13 17:52
 **/
@Slf4j
@RestController
@Api(tags = "智能导入API")
public class IntelligentImportController {

    @Resource
    private IIntelligentImportService intelligentImportService;

    @PostMapping(value = "/intelligent/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "智能导入数据，符合模板直接导入，不符合返回列及映射关系")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "File"),
            @ApiImplicitParam(name = "importType", value = "导入类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "cover", value = "是否覆盖数据", dataType = "Boolean")
    })
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("importType") Integer importType,
                                                        @RequestParam("cover") boolean cover) throws Exception {
        IntelligentImportVO importVO = new IntelligentImportVO();
        importVO.setUserId(UserUtil.getUserId());
        importVO.setRoleCode(UserUtil.getRoleCode());
        importVO.setSchoolId(UserUtil.getSchoolIdByDefault());
        importVO.setAreaCode(UserUtil.getAreaCodeByDefault());
        importVO.setImportType(importType);
        importVO.setCover(cover);

        ImportRecord importRecord = intelligentImportService.importData(file, importVO);
        Map<String, Object> result = new HashMap<>(4);
        if (importRecord.getIsTemplate()) {
            result.put("totalNum", importRecord.getTotalNum());
            result.put("successNum", importRecord.getSuccessNum());
            result.put("failNum", importRecord.getFailNum());
            result.put("failPath", importRecord.getFailPath());
            result.put("isTemplate", importRecord.getIsTemplate());
        } else {
            result.put("header", importRecord.getHeader());
            result.put("columnMapping", importRecord.getColumnMapping());
            result.put("templateFilePath", importRecord.getTempFilePath());
        }
        return success(result);
    }

    @PostMapping(value = "/intelligent/importDataWithHeaders", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "智能导入数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "filePath", value = "文件路径", required = true, dataType = "String"),
            @ApiImplicitParam(name = "importType", value = "导入类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "columnMapping", value = "列与属性映射", required = true, dataType = "String"),
            @ApiImplicitParam(name = "cover", value = "是否覆盖数据", dataType = "Boolean")
    })
    public CommonResult<Map<String, Object>> importDataWithHeaders(@RequestParam("filePath") String filePath,
                                                                   @RequestParam("importType") Integer importType,
                                                                   @RequestParam("columnMapping") String columnMapping,
                                                                   @RequestParam("cover") boolean cover) throws Exception {
        IntelligentImportVO importVO = new IntelligentImportVO();
        importVO.setUserId(UserUtil.getUserId());
        importVO.setRoleCode(UserUtil.getRoleCode());
        importVO.setSchoolId(UserUtil.getSchoolIdByDefault());
        importVO.setAreaCode(UserUtil.getAreaCodeByDefault());
        importVO.setImportType(importType);
        importVO.setColumnMapping(columnMapping);
        importVO.setCover(cover);

        ImportRecord importRecord = intelligentImportService.importDataWithHeaders(filePath, importVO);
        Map<String, Object> result = new HashMap<>(4);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }
}