package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.dto.AreaTeacherDTO;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherRespVO;
import com.cnasoft.health.userservice.feign.dto.UserReqVO;
import com.cnasoft.health.userservice.model.AreaTeacher;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IAreaTeacherService;
import com.cnasoft.health.userservice.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.groups.Default;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * @author lqz-ganghe
 * 2022/4/15
 * 区域心理教研员管理控制器
 */
@Slf4j
@RestController
@Api(tags = "区域心理教研员API")
public class AreaTeacherController extends AbstractImportController {
    @Resource
    private IAreaTeacherService areaTeacherService;

    /**
     * 指定装配类
     */
    @Resource(name = "areaTeacherImportServiceImpl")
    private IImportDataInterface<AreaTeacherDTO> areaTeacherImport;

    @Override
    @PostMapping(value = "/areaTeacher/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入区域心理教研员数据")
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Integer areaCode = UserUtil.getAreaCode();

        //指定模板表头
        ImportParam<AreaTeacherDTO> importParam =
            new ImportParam.Builder<AreaTeacherDTO>(cover, ImportTypeEnum.AREA_TEACHER).setFileService(new FileServiceImpl<>()).setExcelLoadService(new ExcelLoadServiceImpl<>())
                .setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file).setCover(cover).setDateInterface(Date::new)
                .setUserId(userId).setCreateBy(userId).setAreaCode(areaCode).setUpdateBy(userId).build();

        //导入Excel数据，返回已经验证了数据格式的数据，并验证了数据重复
        ImportRecord importRecord = areaTeacherImport.importExcel(importParam, false, null);
        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    @GetMapping("/areaTeacher/list")
    @ApiOperation(value = "查询区域心理教研员列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "查询条件", dataType = "String"),
        @ApiImplicitParam(name = "enabled", value = "状态(启用/禁用)", dataType = "Boolean")})
    public CommonResult<PageResult<AreaTeacherRespVO>> list(@RequestParam Map<String, Object> params) {
        return CommonResult.success(areaTeacherService.findList(params));
    }

    @PostMapping("/areaTeacher/info")
    @ApiOperation(value = "新增区域心理教研员")
    public CommonResult<AreaTeacherDTO> saveAreaTeacher(@RequestBody @Validated({UserReqVO.Add.class, Default.class}) AreaTeacherReqVO createReqVO) throws Exception {
        areaTeacherService.saveAreaTeacher(createReqVO);
        return success();
    }

    @PutMapping("/areaTeacher/info")
    @ApiOperation(value = "更新区域心理教研员信息")
    public CommonResult<AreaTeacherDTO> updateAreaTeacher(@RequestBody @Validated({UserReqVO.Update.class, Default.class}) AreaTeacherReqVO updateReqVO) throws Exception {
        areaTeacherService.updateAreaTeacher(updateReqVO);
        return success();
    }

    @DeleteMapping("/areaTeacher/info")
    @ApiOperation(value = "删除区域心理教研员")
    public CommonResult<List<BatchOperationTipDTO>> deleteAreaTeacher(@RequestParam Set<Long> ids) {
        return success(areaTeacherService.deleteAreaTeacher(ids));
    }

    @GetMapping("/areaTeacher/info")
    @ApiOperation(value = "区域心理教研员详情")
    public CommonResult<AreaTeacherRespVO> getAreaTeacherInfo(@RequestParam Long userId) {
        return success(areaTeacherService.findByUserId(userId));
    }

    @GetMapping("/areaTeacher/current/info")
    @ApiOperation(value = "当前登录用户区域心理教研员详情")
    public CommonResult<AreaTeacherRespVO> getAreaTeacherInfo() {
        Long userId = UserUtil.getUserId();
        return success(areaTeacherService.findByUserId(userId));
    }

    @PutMapping("/areaTeacher/current/info")
    @ApiOperation(value = "更新当前区域心理教研员")
    public CommonResult<Object> updateCurrentAreaTeacher(@RequestBody @Validated AreaTeacherReqVO teacherReqVO) throws Exception {
        areaTeacherService.updateCurrentAreaTeacher(teacherReqVO);
        return success();
    }

    @GetMapping("/areaTeacher/findTaskHandlerIdByAreaCode")
    @ApiOperation(value = "根据区域code查找区域承接任务的心理教研员的用户id")
    public CommonResult<Long> findTaskHandlerIdByAreaCode(@RequestParam Integer areaCode) {
        return success(areaTeacherService.findTaskHandlerIdByAreaCode(areaCode));
    }

    @GetMapping("/areaTeacher/findAreaTeacherInfo")
    @ApiOperation(value = "根据用户id查询区域心理教研员信息")
    public CommonResult<com.cnasoft.health.common.dto.AreaTeacherDTO> findAreaTeacherInfo(@RequestParam Long userId) {
        return success(areaTeacherService.findAreaTeacherInfo(userId));
    }

    @GetMapping("/areaTeacher/getAreaPsychoTeacher")
    @ApiOperation(value = "查询区域心理教研员数据")
    public CommonResult<List<CommonDTO>> getAreaPsychoTeacher(@RequestParam Integer areaCode) {
        return success(areaTeacherService.getAreaPsychoTeacher(areaCode));
    }
}
