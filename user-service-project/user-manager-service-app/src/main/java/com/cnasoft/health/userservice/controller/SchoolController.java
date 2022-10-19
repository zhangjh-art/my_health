package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveRecord;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SchoolReqVO;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.service.ISchoolService;
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

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.success;

/**
 * @author lqz
 * @date 2022/4/12
 * 学校管理
 */
@Slf4j
@RestController
@Api(tags = "学校API")
public class SchoolController {

    @Resource
    private ISchoolService schoolService;

    @GetMapping("/school/list")
    @ApiOperation(value = "分页获取学校列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "query", value = "学校名称", dataType = "String")})
    public CommonResult<PageResult<SchoolDTO>> listSchool(@RequestParam Map<String, Object> params) {
        return success(schoolService.listSchool(params));
    }

    @GetMapping("/school/listAll")
    @ApiOperation(value = "获取所有的学校列表")
    public CommonResult<List<SchoolDTO>> listAll(@RequestParam(required = false, value = "areaCodes") List<Integer> areaCodes) {
        return success(schoolService.listAllSchool(areaCodes));
    }

    @GetMapping("/school/list/server")
    @ApiOperation(value = "根据学校id列表获取学校信息列表")
    public CommonResult<List<SchoolDTO>> getSchoolListByIds(@RequestBody Set<Long> ids) {
        return success(schoolService.getSchoolListByIds(ids));
    }

    @GetMapping("/school/detail")
    @ApiOperation(value = "获取学校详情")
    public CommonResult<SchoolDTO> getSchoolDetail(@RequestParam Long id) {
        return success(schoolService.getSchoolDetail(id));
    }

    @PostMapping("/school/info")
    @ApiOperation(value = "新增学校数据")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_SCHOOL)
    public CommonResult<Object> saveSchool(@RequestBody @Validated SchoolReqVO vo) {
        School school = schoolService.saveSchool(vo);
        return success(school);
    }

    @PutMapping("/school/info")
    @ApiOperation(value = "更新学校数据")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_SCHOOL)
    public CommonResult<Object> updateSchool(@RequestBody @Validated(SchoolReqVO.Update.class) SchoolReqVO vo) {
        schoolService.updateSchool(vo);
        return success();
    }

    @DeleteMapping("/school/info")
    @ApiOperation(value = "删除指定学校")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_SCHOOL)
    public CommonResult<List<BatchOperationTipDTO>> deleteSchool(@RequestParam Set<Long> ids) {
        return success(schoolService.deleteSchool(ids));
    }

    @GetMapping("/school/list/ids")
    CommonResult<List<CommonDTO>> getSchoolByIds(@RequestParam Set<Long> schoolIds) {
        return success(schoolService.listSchool(schoolIds));
    }

    @GetMapping("/school/statistics")
    CommonResult<List<Map<String, Object>>> getSchoolStatistics() {
        return success(schoolService.getSchoolStatistics());
    }
}
