package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.UserReqVO;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SchoolStaff;
import com.cnasoft.health.userservice.service.ISchoolStaffService;
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
import springfox.documentation.annotations.ApiIgnore;

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
 * 校教职工
 *
 * @author zcb
 * @date 2022-04-19
 */
@Slf4j
@RestController
@Api(tags = "校教职工API")
public class SchoolStaffController extends AbstractImportController {

    /**
     * 指定装配类
     */
    @Resource(name = "schoolStaffImportServiceImpl")
    private IImportDataInterface<com.cnasoft.health.userservice.excel.dto.SchoolStaffDTO> schoolStaffService;

    @Resource
    private ISchoolStaffService staffService;

    @Override
    @PostMapping(value = "/schoolStaff/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入校职工数据")
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Long schoolId = UserUtil.getSchoolId();
        Integer areaCode = UserUtil.getAreaCode();

        //指定模板表头
        ImportParam<com.cnasoft.health.userservice.excel.dto.SchoolStaffDTO> importParam =
            new ImportParam.Builder<com.cnasoft.health.userservice.excel.dto.SchoolStaffDTO>(cover, ImportTypeEnum.SCHOOL_STAFF).setFileService(new FileServiceImpl<>())
                .setExcelLoadService(new ExcelLoadServiceImpl<>()).setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file)
                .setAreaCode(areaCode).setCover(cover).setDateInterface(Date::new).setUserId(userId).setCreateBy(userId).setSchoolId(schoolId).setUpdateBy(userId).build();

        //导入Excel数据，返回已经验证了数据格式的数据，并验证了数据重复
        ImportRecord importRecord = schoolStaffService.importExcel(importParam, false, null);

        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    @GetMapping("/schoolStaff/list")
    @ApiOperation(value = "获取校教职工列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "查询条件", dataType = "String"),
        @ApiImplicitParam(name = "enabled", value = "状态(启用/禁用)", dataType = "Boolean")})
    public CommonResult<PageResult<SchoolStaffRespVO>> list(@RequestParam Map<String, Object> params) throws Exception {
        return success(staffService.findList(params));
    }

    @PostMapping("/schoolStaff/info")
    @ApiOperation(value = "新增校教职工")
    public CommonResult<SchoolStaff> addSchoolStaff(@RequestBody @Validated({UserReqVO.Add.class, Default.class}) SchoolStaffReqVO staffReqVO) throws Exception {
        staffService.add(staffReqVO);
        return success();
    }

    @PutMapping("/schoolStaff/info")
    @ApiOperation(value = "编辑校教职工")
    public CommonResult<SchoolStaff> updateSchoolStaff(@RequestBody @Validated({UserReqVO.Update.class, Default.class}) SchoolStaffReqVO staffReqVO) throws Exception {
        staffService.update(staffReqVO);
        return success();
    }

    @DeleteMapping("/schoolStaff/info")
    @ApiOperation(value = "删除校教职工")
    public CommonResult<List<BatchOperationTipDTO>> deleteSchoolStaff(@RequestParam Set<Long> ids) throws Exception {
        return success(staffService.delete(ids));
    }

    @GetMapping("/schoolStaff/headerTeacherList")
    @ApiOperation(value = "班主任列表")
    public CommonResult<List<CommonDTO>> headerTeacherList() {
        return success(staffService.headerTeacherList());
    }

    @GetMapping("/schoolStaff/info")
    @ApiOperation(value = "教职工详情")
    public CommonResult<SchoolStaffRespVO> getTeacherInfo(@RequestParam Long userId) {
        return success(staffService.findByUserId(userId));
    }

    @GetMapping("/schoolStaff/current/info")
    @ApiOperation(value = "当前登录教职工详情")
    public CommonResult<SchoolStaffRespVO> getSchoolStaffInfo() {
        Long userId = UserUtil.getUserId();
        return success(staffService.findByUserId(userId));
    }

    @PutMapping("/schoolStaff/current/info")
    @ApiOperation(value = "更新当前教职工信息")
    public CommonResult<Object> updateCurrentSchoolStaff(@RequestBody @Validated SchoolStaffReqVO staffReqVO) throws Exception {
        staffService.updateCurrentSchoolStaff(staffReqVO);
        return success();
    }

    @GetMapping("/schoolStaff/mentalfile/list")
    @ApiOperation(value = "获取校教职工心理档案列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "姓名", dataType = "String"),
        @ApiImplicitParam(name = "department", value = "部门", dataType = "String")})
    public CommonResult<PageResult<StaffMentalFileVO>> listMentalFile(@ApiIgnore @RequestParam Map<String, Object> params) throws Exception {
        return success(staffService.listMentalFile(params));
    }

    @GetMapping("/schoolStaff/info/union")
    @ApiOperation(value = "校职员详情,包含校心理教师信息")
    public CommonResult<UserRespVO> getAreaStaffInfoUnion(@RequestParam Long userId) {
        return success(staffService.findUnionUser(userId));
    }

    @GetMapping("/schoolStaff/getSchoolStaffUserIdBySchoolAndDepartmentCode")
    @ApiOperation(value = "根据学校id和部门code列表获取教职工（包含心理老师）用户id列表")
    public CommonResult<List<UserClazzVO>> getSchoolStaffUserIdBySchoolAndDepartmentCode(@RequestParam Long schoolId,
        @RequestParam(required = false) List<String> departmentCodes) {
        return success(staffService.getSchoolStaffUserIdBySchoolAndDepartmentCode(schoolId, departmentCodes));
    }

    @GetMapping("/schoolStaff/findSchoolStaffInfo")
    @ApiOperation(value = "根据用户id获取校教职工数据")
    public CommonResult<com.cnasoft.health.common.dto.SchoolStaffDTO> findSchoolStaffInfo(@RequestParam Long userId) {
        return success(staffService.findSchoolStaffInfo(userId));
    }

    @PostMapping("/schoolStaff/findSchoolTeacherStaffList")
    @ApiOperation(value = "根据用户id查询校心理老师和教职工数据")
    public CommonResult<List<com.cnasoft.health.common.dto.SchoolTeacherStaffDTO>> findSchoolTeacherStaffList(@RequestBody Set<Long> userIds) {
        return success(staffService.findSchoolTeacherStaffList(userIds));
    }
}
