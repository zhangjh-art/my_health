package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.dto.SchoolTeacherDTO;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherRespVO;
import com.cnasoft.health.userservice.feign.dto.UserReqVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SchoolTeacher;
import com.cnasoft.health.userservice.service.ISchoolTeacherService;
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
 * 校心理老师
 *
 * @author liukl-ganghe
 * @date 2022-04-10
 */
@Slf4j
@RestController
@Api(tags = "校心理老师API")
public class SchoolTeacherController extends AbstractImportController {

    /**
     * 指定装配类
     */
    @Resource(name = "schoolTeacherImportServiceImpl")
    private IImportDataInterface<SchoolTeacherDTO> schoolTeacherService;

    @Resource
    private ISchoolTeacherService teacherService;

    @Override
    @PostMapping(value = "/schoolTeacher/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入校心理老师")
    @ApiImplicitParams({@ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "file"),
        @ApiImplicitParam(name = "cover", value = "是否覆盖", required = true, dataType = "boolean"),})
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Long schoolId = UserUtil.getSchoolId();
        Integer areaCode = UserUtil.getAreaCode();

        //指定模板表头
        ImportParam<SchoolTeacherDTO> importParam = new ImportParam.Builder<SchoolTeacherDTO>(cover, ImportTypeEnum.SCHOOL_TEACHER).setFileService(new FileServiceImpl<>())
            .setExcelLoadService(new ExcelLoadServiceImpl<>()).setAreaCode(areaCode).setUserId(userId).setSchoolId(schoolId).setCreateBy(userId).setUpdateBy(userId)
            .setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file).setCover(cover).setDateInterface(Date::new).build();

        //导入Excel数据，返回已经验证了数据格式的数据，并验证了数据重复
        ImportRecord importRecord = schoolTeacherService.importExcel(importParam, false, null);
        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    @GetMapping("/schoolTeacher/list")
    @ApiOperation(value = "获取校心理老师列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "查询条件", dataType = "String"),
        @ApiImplicitParam(name = "enabled", value = "状态(启用/禁用)", dataType = "Boolean")})
    public CommonResult<PageResult<SchoolTeacherRespVO>> list(@RequestParam Map<String, Object> params) throws Exception {
        return success(teacherService.findList(params));
    }

    @PostMapping("/schoolTeacher/info")
    @ApiOperation(value = "新增校心理老师")
    public CommonResult<SchoolTeacher> addSchoolTeacher(@RequestBody @Validated({UserReqVO.Add.class, Default.class}) SchoolTeacherReqVO teacherReqVO) throws Exception {
        teacherService.add(teacherReqVO);
        return success();
    }

    @PutMapping("/schoolTeacher/info")
    @ApiOperation(value = "编辑校心理老师")
    public CommonResult<SchoolTeacher> updateSchoolTeacher(@RequestBody @Validated({UserReqVO.Update.class, Default.class}) SchoolTeacherReqVO teacherReqVO) throws Exception {
        teacherService.update(teacherReqVO);
        return success();
    }

    @DeleteMapping("/schoolTeacher/info")
    @ApiOperation(value = "删除校心理老师")
    public CommonResult<List<BatchOperationTipDTO>> deleteSchoolTeacher(@RequestParam Set<Long> ids) throws Exception {
        return success(teacherService.delete(ids));
    }

    @GetMapping("/schoolTeacher/info")
    @ApiOperation(value = "校心理老师详情")
    public CommonResult<SchoolTeacherRespVO> getSchoolTeacherInfo(@RequestParam Long userId) {
        return success(teacherService.findByUserId(userId));
    }

    @GetMapping("/schoolTeacher/current/info")
    @ApiOperation(value = "当前登录校心理老师详情")
    public CommonResult<SchoolTeacherRespVO> getSchoolTeacherInfo() {
        Long userId = UserUtil.getUserId();
        return success(teacherService.findByUserId(userId));
    }

    @PutMapping("/schoolTeacher/current/info")
    @ApiOperation(value = "更新当前校心理老师")
    public CommonResult<Object> updateCurrentSchoolTeacher(@RequestBody @Validated SchoolTeacherReqVO teacherReqVO) throws Exception {
        teacherService.updateCurrentSchoolTeacher(teacherReqVO);
        return success();
    }

    @GetMapping("/schoolTeacher/findTaskHandlerIdBySchoolId")
    @ApiOperation(value = "根据学校id查找承接任务的学校心理老师的用户id")
    public CommonResult<Long> findTaskHandlerIdBySchoolId(@RequestParam Long schoolId) {
        return success(teacherService.findTaskHandlerIdBySchoolId(schoolId));
    }

    @GetMapping("/schoolTeacher/getSchoolPsychoTeacherSchoolId")
    @ApiOperation(value = "获取心理老师的学校id")
    public CommonResult<Long> getSchoolPsychoTeacherSchoolId(@RequestParam Long userId) {
        return success(teacherService.getSchoolPsychoTeacherSchoolId(userId));
    }

    @GetMapping("/schoolTeacher/findSchoolTeacherInfo")
    @ApiOperation(value = "根据用户id获取校心理老师数据")
    public CommonResult<com.cnasoft.health.common.dto.SchoolTeacherDTO> findSchoolTeacherInfo(@RequestParam Long userId) {
        return success(teacherService.findSchoolTeacherInfo(userId));
    }

    @GetMapping("/schoolTeacher/getSchoolPsychoTeacher")
    @ApiOperation(value = "查询校心理老师数据")
    public CommonResult<List<CommonDTO>> getSchoolPsychoTeacher(@RequestParam Long schoolId) {
        return success(teacherService.getSchoolPsychoTeacher(schoolId));
    }
}
