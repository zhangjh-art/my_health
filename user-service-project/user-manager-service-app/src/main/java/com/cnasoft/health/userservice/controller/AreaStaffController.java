package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.dto.AreaStaffDTO;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.AreaStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.UserReqVO;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IAreaStaffService;
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
 * @author lqz
 * 2022/4/15
 * 区域职员管理控制器
 */
@Slf4j
@RestController
@Api(tags = "区域职员模块API")
public class AreaStaffController extends AbstractImportController {

    @Resource
    private IAreaStaffService areaStaffService;

    /**
     * 指定装配类
     */
    @Resource(name = "areaStaffImportServiceImpl")
    private IImportDataInterface<AreaStaffDTO> areaStaffImport;

    @Override
    @PostMapping(value = "/areaStaff/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入区域职员数据")
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Integer areaCode = UserUtil.getAreaCode();
        //指定模板表头
        ImportParam<AreaStaffDTO> importParam =
            new ImportParam.Builder<AreaStaffDTO>(cover, ImportTypeEnum.AREA_STAFF).setFileService(new FileServiceImpl<>()).setExcelLoadService(new ExcelLoadServiceImpl<>())
                .setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file).setAreaCode(areaCode).setCover(cover)
                .setDateInterface(Date::new).setUserId(userId).setCreateBy(userId).setUpdateBy(userId).build();

        //导入Excel数据，返回已经验证了数据格式的数据，并验证了数据重复
        ImportRecord importRecord = areaStaffImport.importExcel(importParam, false, null);
        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    @GetMapping("/areaStaff/list")
    @ApiOperation(value = "查询区域职员列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "ID/权限名称", dataType = "String"),
        @ApiImplicitParam(name = "enabled", value = "启用/禁用状态", dataType = "Boolean")})
    public CommonResult<PageResult<AreaStaffRespVO>> list(@RequestParam Map<String, Object> params) {
        return CommonResult.success(areaStaffService.findList(params));
    }

    @PostMapping("/areaStaff/info")
    @ApiOperation(value = "新增区域职员")
    public CommonResult<Object> saveAreaStaff(@RequestBody @Validated({UserReqVO.Add.class, Default.class}) AreaStaffReqVO staffReqVO) throws Exception {
        areaStaffService.saveAreaStaff(staffReqVO);
        return success();
    }

    @PutMapping("/areaStaff/info")
    @ApiOperation(value = "更新区域职员")
    public CommonResult<Object> updateAreaStaff(@RequestBody @Validated({UserReqVO.Update.class, Default.class}) AreaStaffReqVO staffReqVO) throws Exception {
        areaStaffService.updateAreaStaff(staffReqVO);
        return success();
    }

    @DeleteMapping("/areaStaff/info")
    @ApiOperation(value = "删除区域职员")
    public CommonResult<List<BatchOperationTipDTO>> deleteAreaStaff(@RequestParam Set<Long> ids) {
        return success(areaStaffService.deleteAreaStaff(ids));
    }

    @GetMapping("/areaStaff/info")
    @ApiOperation(value = "区域职员详情")
    public CommonResult<AreaStaffRespVO> getAreaStaffInfo(@RequestParam Long userId) {
        return success(areaStaffService.findByUserId(userId));
    }

    @GetMapping("/areaStaff/current/info")
    @ApiOperation(value = "当前登录用户区域职员详情")
    public CommonResult<AreaStaffRespVO> getAreaStaffInfo() throws Exception {
        Long userId = UserUtil.getUserId();
        return success(areaStaffService.findByUserId(userId));
    }

    @PutMapping("/areaStaff/current/info")
    @ApiOperation(value = "更新当前区域职员")
    public CommonResult<Object> updateCurrentAreaStaff(@RequestBody @Validated AreaStaffReqVO staffReqVO) throws Exception {
        areaStaffService.updateCurrentAreaStaff(staffReqVO);
        return success();
    }

    @GetMapping("/areaStaff/mentalfile/list")
    @ApiOperation(value = "获取区域职员心理档案列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "姓名", dataType = "String")})
    public CommonResult<PageResult<StaffMentalFileVO>> listMentalFile(@ApiIgnore @RequestParam Map<String, Object> params) {
        return success(areaStaffService.listMentalFile(params));
    }

    @GetMapping("/areaStaff/info/union")
    @ApiOperation(value = "区域职员详情,包含区域教师信息")
    public CommonResult<UserRespVO> getAreaStaffInfoUnion(@RequestParam Long userId) {
        return success(areaStaffService.findUnionUser(userId));
    }

    @GetMapping("/areaStaff/findAreaStaffInfo")
    @ApiOperation(value = "获取区域职员信息")
    public CommonResult<com.cnasoft.health.common.dto.AreaStaffDTO> findAreaStaffInfo(@RequestParam Long userId) {
        return success(areaStaffService.findAreaStaffInfo(userId));
    }
}
