package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.StudentBaseInfoDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.dto.StudentDTO;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.StudentBaseInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseReqVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentSaveVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IStudentBaseInfoService;
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
 * TODO 学生数据全部加密
 * 学生接口
 *
 * @author zcb
 * @date 2022-03-10
 */
@Slf4j
@RestController
@Api(tags = "学生API")
public class StudentController extends AbstractImportController {

    @Resource
    private IStudentBaseInfoService studentService;

    /**
     * 指定装配类
     */
    @Resource(name = "studentImportServiceImpl")
    private IImportDataInterface<StudentDTO> studentImport;

    @Override
    @PostMapping(value = "/student/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入学生数据")
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Long schoolId = UserUtil.getSchoolId();
        Integer areaCode = UserUtil.getAreaCode();

        //指定模板表头
        ImportParam<StudentDTO> importParam =
            new ImportParam.Builder<StudentDTO>(cover, ImportTypeEnum.STUDENT).setFileService(new FileServiceImpl<>()).setExcelLoadService(new ExcelLoadServiceImpl<>())
                .setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file).setAreaCode(areaCode).setCover(cover)
                .setDateInterface(Date::new).setSchoolId(schoolId).setUserId(userId).setCreateBy(userId).setUpdateBy(userId).build();

        ImportRecord importRecord = studentImport.importExcel(importParam, false, null);
        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    @PostMapping(value = "/student/create")
    @ApiOperation(value = "新增学生")
    public CommonResult<Object> create(@RequestBody @Validated({StudentSaveVO.Add.class, Default.class}) StudentSaveVO student) throws Exception {
        studentService.create(student);
        return success();
    }

    @PostMapping(value = "/student/update")
    @ApiOperation(value = "编辑学生")
    public CommonResult<Object> update(@RequestBody @Validated({StudentSaveVO.Update.class, Default.class}) StudentSaveVO student) throws Exception {
        studentService.updateStudent(student);
        return success();
    }

    @DeleteMapping(value = "/student/delete")
    @ApiOperation(value = "删除学生")
    public CommonResult<List<BatchOperationTipDTO>> delete(@RequestParam Set<Long> ids) {
        return success(studentService.delete(ids));
    }

    @GetMapping(value = "/student/list")
    @ApiOperation(value = "查询学生列表")
    public CommonResult<PageResult<StudentRespVO>> list(@RequestParam Map<String, Object> params) {
        return success(studentService.list(params));
    }

    @GetMapping(value = "/student/info")
    @ApiOperation(value = "根据学生id查询学生详情")
    public CommonResult<StudentInfoRespVO> info(@RequestParam Long userId) {
        return success(studentService.info(userId));
    }

    @GetMapping(value = "/student/base/current/info")
    @ApiOperation(value = "当前登录用户学生基础信息")
    public CommonResult<StudentBaseInfoRespVO> baseInfo() {
        Long userId = UserUtil.getUserId();
        return success(studentService.baseInfo(userId));
    }

    @PutMapping("/student/base/current/info")
    @ApiOperation(value = "更新当前学生基础信息")
    public CommonResult<Object> updateCurrentStudent(@RequestBody @Validated StudentBaseReqVO studentReqVO) throws Exception {
        studentService.updateCurrentStudent(studentReqVO);
        return success();
    }

    @GetMapping(value = "/student/list/ids")
    @ApiOperation(value = "根据用户id列表查询学生基础信息")
    public CommonResult<List<StudentBaseInfoDTO>> getStudentListByUserIds(@RequestBody Set<Long> userIds) {
        return success(studentService.getStudentListByUserIds(userIds));
    }

    @GetMapping(value = "/student/list/query")
    @ApiOperation(value = "根据姓名、学号、年级、班级、测试管理员用户id查询学生用户id列表")
    public CommonResult<List<Long>> getUserIdsByQuery(@RequestParam Map<String, Object> params) {
        return success(studentService.getUserIdsByQuery(params));
    }

    @GetMapping(value = "/student/list/name")
    @ApiOperation(value = "根据姓名查询学生用户id列表")
    public CommonResult<List<Long>> getUserIdsByName(@RequestParam String name) {
        return success(studentService.getUserIdsByName(name));
    }

    @GetMapping(value = "/student/mentalfile/list")
    @ApiOperation(value = "分页查询学生心理档案")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "姓名", dataType = "String"),
        @ApiImplicitParam(name = "grade", value = "年纪code", paramType = "query", required = true, dataType = "string"),
        @ApiImplicitParam(name = "clazzId", value = "班级id", paramType = "query", required = true, dataType = "long"),
        @ApiImplicitParam(name = "studentStatus", value = "学籍状态：1=在读，2=毕业，3=转校，4=休学，5=退学，6=肄业", dataType = "Boolean")})
    public CommonResult<PageResult<StudentBaseRespVO>> listBaseInfo(@ApiIgnore @RequestParam Map<String, Object> params) {
        return success(studentService.listBaseInfo(params));
    }

    @PostMapping(value = "/student/getStudentUserIdByParams")
    @ApiOperation(value = "根据条件查询学生用户id")
    public CommonResult<List<Long>> getStudentUserIdByParams(@RequestBody Map<String, Object> params) {
        return success(studentService.getStudentUserIdByParams(params));
    }

    @GetMapping(value = "/student/getStudentUserIdClazzIdBySchoolAndClass")
    @ApiOperation(value = "根据学校和班级查询学生用户id和班级id")
    public CommonResult<List<UserClazzVO>> getStudentUserIdClazzIdBySchoolAndClass(@RequestParam Long schoolId, @RequestParam List<Long> clazzIds) {
        return success(studentService.getStudentUserIdClazzIdBySchoolAndClass(schoolId, clazzIds));
    }

    @PostMapping(value = "/student/findStudentListByIds")
    @ApiOperation(value = "根据用户id查询学生基本信息")
    public CommonResult<List<com.cnasoft.health.common.dto.StudentDTO>> findStudentListByIds(@RequestBody Set<Long> userIds) {
        return success(studentService.findStudentListByIds(userIds));
    }

    @GetMapping(value = "/student/findStudentGradeAndIDNumber")
    @ApiOperation(value = "获取学生的年级和身份证号")
    public CommonResult<com.cnasoft.health.common.dto.StudentDTO> findStudentGradeAndIDNumber(@RequestParam Long userId) {
        return success(studentService.findStudentGradeAndIDNumber(userId));
    }

    @GetMapping(value = "/student/findStudentInfo")
    @ApiOperation(value = "根据用户id查询学生基本信息")
    public CommonResult<com.cnasoft.health.common.dto.StudentDTO> findStudentInfo(@RequestParam Long userId) {
        return success(studentService.findStudentInfo(userId));
    }

    @GetMapping(value = "/student/findStudentUserIdByParentUserId")
    @ApiOperation(value = "根据用户id查询学生基本信息")
    public CommonResult<List<Long>> findStudentUserIdByParentUserId(@RequestParam Long userId) {
        return success(studentService.findStudentUserIdByParentUserId(userId));
    }
}
