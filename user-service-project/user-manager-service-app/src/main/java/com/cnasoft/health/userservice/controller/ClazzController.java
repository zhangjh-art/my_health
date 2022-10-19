package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.ClazzReqDTO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * @author lqz
 * 2022/4/14
 * 班级管理控制器
 */
@Slf4j
@RestController
@Api(tags = "班级API")
public class ClazzController extends AbstractImportController {

    @Resource
    private IClazzService clazzService;

    /**
     * 指定装配类
     */
    @Resource(name = "clazzImportServiceImpl")
    private IImportDataInterface<com.cnasoft.health.userservice.excel.dto.ClazzDTO> clazzImportService;

    @Override
    @PostMapping(value = "/class/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入班级数据")
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Long schoolId = UserUtil.getSchoolId();

        //指定模板表头
        ImportParam<com.cnasoft.health.userservice.excel.dto.ClazzDTO> importParam =
            new ImportParam.Builder<com.cnasoft.health.userservice.excel.dto.ClazzDTO>(cover, ImportTypeEnum.CLAZZ).setFileService(new FileServiceImpl<>())
                .setExcelLoadService(new ExcelLoadServiceImpl<>()).setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file)
                .setCover(cover).setDateInterface(Date::new).setUserId(userId).setCreateBy(userId).setSchoolId(schoolId).setUpdateBy(userId).build();

        //导入Excel数据，返回已经验证了数据格式的数据，并验证了数据重复
        ImportRecord importRecord = clazzImportService.importExcel(importParam, false, null);
        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    @GetMapping("/class/list")
    @ApiOperation(value = "分页查询当前学校的所有班级列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "查询条件", dataType = "String"),
        @ApiImplicitParam(name = "isGraduated", value = "是否已毕业", dataType = "Boolean")})
    public CommonResult<PageResult<ClazzDTO>> list(@RequestParam Map<String, Object> params) {
        return CommonResult.success(clazzService.findList(params));
    }

    @GetMapping("/class/listAll")
    @ApiOperation(value = "查询所有班级列表")
    public CommonResult<List<ClazzDTO>> listAll(@RequestParam(required = false) Long schoolId) {
        if (Objects.isNull(schoolId)) {
            schoolId = UserUtil.getSchoolId();
        }
        return success(clazzService.listAll(schoolId));
    }

    @GetMapping("/class/list/grade")
    @ApiOperation(value = "获取学校指定年级的班级列表")
    public CommonResult<List<ClazzDTO>> listClazzByGrade(@RequestParam String grade) {
        return CommonResult.success(clazzService.listClazzByGrade(grade));
    }

    @GetMapping("/class/list/class")
    @ApiOperation(value = "获取指定班级名的班级列表")
    public CommonResult<List<ClazzDTO>> listClazzByClazzName(@RequestParam String clazzName) {
        return CommonResult.success(clazzService.listClazzByClazzName(clazzName));
    }

    @GetMapping("/class/detail")
    @ApiOperation(value = "根据年级和班号获得班级信息")
    public CommonResult<ClazzDTO> getClazzDetail(@RequestParam String grade, @RequestParam String clazzName) {
        return CommonResult.success(clazzService.getClazzDetail(grade, clazzName));
    }

    @PostMapping("/class/info")
    @ApiOperation(value = "新增班级")
    public CommonResult<Object> saveClazz(@RequestBody @Validated ClazzReqDTO clazzDTO) {
        clazzService.saveClazz(clazzDTO);
        return CommonResult.success();
    }

    @PutMapping("/class/info")
    @ApiOperation(value = "更新班级信息")
    public CommonResult<Object> updateClazz(@RequestBody @Validated(ClazzReqDTO.Update.class) ClazzReqDTO clazzDTO) {
        clazzService.updateClazz(clazzDTO);
        return CommonResult.success();
    }

    @DeleteMapping("/class/info")
    @ApiOperation(value = "删除班级信息")
    public CommonResult<List<BatchOperationTipDTO>> deleteClazz(@RequestParam Set<Long> ids) {
        return success(clazzService.deleteClazz(ids));
    }

    @GetMapping("/class/getListByHeaderTeacher")
    @ApiOperation(value = "获取班主任管理的班级信息")
    CommonResult<List<ClazzDTO>> getListByHeaderTeacher() {
        return success(clazzService.getListByHeaderTeacher());
    }

    @GetMapping("/class/list/server")
    @ApiOperation(value = "根据指定班级id列表获取班级信息")
    CommonResult<List<ClazzDTO>> getClazzListByIds(@RequestBody Set<Long> ids) {
        return success(clazzService.getListByIds(ids));
    }

    @GetMapping("/class/check/headerTeacherWithStudent")
    @ApiOperation(value = "判断班主任管理的班级中是否有该学生")
    CommonResult<Boolean> checkHeaderTeacherWithStudent(@RequestParam Long headerTeacherUserId, @RequestParam Long studentUserId) {
        return success(clazzService.checkHeaderTeacherWithStudent(headerTeacherUserId, studentUserId));
    }

    @GetMapping("/class/getClazzBySchoolIdAndGrade")
    @ApiOperation(value = "根据学校id和年级code获取所有班级列表")
    CommonResult<List<Long>> getClazzBySchoolIdAndGrade(@RequestParam("schoolId") Long schoolId, @RequestParam("grade") String grade) {
        return success(clazzService.getClazzBySchoolIdAndGrade(schoolId, grade));
    }
}
