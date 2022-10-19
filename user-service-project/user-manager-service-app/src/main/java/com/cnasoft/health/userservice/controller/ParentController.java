package com.cnasoft.health.userservice.controller;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.dto.ParentDTO;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.BindStudentVO;
import com.cnasoft.health.userservice.feign.dto.ParentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.ParentBindStudentVO;
import com.cnasoft.health.userservice.feign.dto.ParentReqVO;
import com.cnasoft.health.userservice.feign.dto.ParentRespVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IParentService;
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
import javax.validation.Valid;
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
 * TODO 家长数据全部加密
 * 家长接口
 *
 * @author zcb
 * @date 2022-03-10
 */
@Slf4j
@RestController
@Api(tags = "家长API")
public class ParentController extends AbstractImportController {

    @Resource
    private IParentService parentService;

    /**
     * 指定装配类
     */
    @Resource(name = "parentImportServiceImpl")
    private IImportDataInterface<ParentDTO> parentImportService;

    @Override
    @PostMapping(value = "/parent/importData", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "导入家长数据")
    public CommonResult<Map<String, Object>> importData(@RequestParam("file") MultipartFile file, @RequestParam("cover") boolean cover) throws Exception {
        Long userId = UserUtil.getUserId();
        Long schoolId = UserUtil.getSchoolId();
        Integer areaCode = UserUtil.getAreaCode();

        //指定模板表头
        ImportParam<ParentDTO> importParam =
            new ImportParam.Builder<ParentDTO>(cover, ImportTypeEnum.PARENT).setFileService(new FileServiceImpl<>()).setExcelLoadService(new ExcelLoadServiceImpl<>())
                .setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file).setAreaCode(areaCode).setCover(cover)
                .setDateInterface(Date::new).setUserId(userId).setCreateBy(userId).setSchoolId(schoolId).setUpdateBy(userId).build();

        //导入Excel数据，返回已经验证了数据格式的数据，并验证了数据重复
        ImportRecord importRecord = parentImportService.importExcel(importParam, false, null);
        Map<String, Object> result = new HashMap<>(16);
        result.put("totalNum", importRecord.getTotalNum());
        result.put("successNum", importRecord.getSuccessNum());
        result.put("failNum", importRecord.getFailNum());
        result.put("failPath", importRecord.getFailPath());
        return success(result);
    }

    /**
     * 家长列表
     *
     * @param params 查询参数
     * @return 分页数据
     */
    @GetMapping(value = "/parent/list")
    @ApiOperation(value = "家长列表")
    public CommonResult<PageResult<ParentRespVO>> list(@RequestParam Map<String, Object> params) {
        return success(parentService.list(params));
    }

    @PostMapping(value = "/parent/create")
    @ApiOperation(value = "新增家长")
    public CommonResult<List<String>> create(@RequestBody @Validated({ParentReqVO.Add.class, Default.class}) ParentReqVO parent) throws Exception {
        return success(parentService.create(parent));
    }

    @PostMapping(value = "/parent/update")
    @ApiOperation(value = "编辑家长")
    public CommonResult<Object> update(@RequestBody @Validated({ParentReqVO.Update.class, Default.class}) ParentReqVO parent) throws Exception {
        return success(parentService.updateParent(parent));
    }

    @GetMapping(value = "/parent/info")
    @ApiOperation(value = "家长详情")
    public CommonResult<ParentRespVO> info(@RequestParam Long userId) {
        return success(parentService.infoByUserId(userId));
    }

    @DeleteMapping(value = "/parent/delete")
    @ApiOperation(value = "删除家长")
    public CommonResult<List<BatchOperationTipDTO>> delete(@RequestParam Set<Long> ids) {
        return success(parentService.delete(ids));
    }

    @PostMapping(value = "/parent/pcBindStudent")
    @ApiOperation(value = "pc端家长绑定学生")
    public CommonResult<List<String>> pcBindStudent(@RequestBody @Valid ParentBindStudentVO query) {
        List<String> errorList = parentService.pcBindStudents(query);
        if (CollUtil.isEmpty(errorList)) {
            return success(errorList);
        } else {
            return success(errorList, 400);
        }
    }

    @PutMapping(value = "/parent/updateConfirmAndActiveStatus")
    @ApiOperation(value = "更新家长确认状态及激活状态")
    public CommonResult<Object> updateConfirmAndActiveStatus(@RequestParam Long userId) {
        parentService.updateConfirmAndActiveStatus(userId);
        return success();
    }

    @PostMapping(value = "/parent/mobileBindStudent")
    @ApiOperation(value = "移动端家长绑定学生")
    public CommonResult<List<String>> mobileBindStudent(@RequestBody @Validated({ParentBindStudentVO.Mobile.class, Default.class}) ParentBindStudentVO query) {
        return success(parentService.mobileBindStudents(query));
    }

    @GetMapping(value = "/parent/user/info")
    @ApiOperation(value = "根据用户获取家长详情")
    public CommonResult<ParentRespVO> infoByUserId(@RequestParam Long userId) {
        return success(parentService.infoByUserId(userId));
    }

    @GetMapping(value = "/parent/user/current/info")
    @ApiOperation(value = "当前登录家长详情")
    public CommonResult<ParentRespVO> infoByCurrentUser() {
        Long userId = UserUtil.getUserId();
        return success(parentService.infoByUserId(userId));
    }

    @PutMapping("/parent/current/info")
    @ApiOperation(value = "更新当前家长信息")
    public CommonResult<Object> updateCurrentParent(@RequestBody @Validated ParentReqVO parentReqVO) throws Exception {
        parentService.updateCurrentParent(parentReqVO);
        return success();
    }

    @PostMapping(value = "/h5/parent/bindStudent")
    @ApiOperation(value = "h5端家长绑定学生")
    public CommonResult<Object> h5BindStudent(@RequestBody @Validated BindStudentVO vo) {
        List<String> errorList = parentService.h5BindStudent(vo);
        if (CollUtil.isEmpty(errorList)) {
            return success(errorList);
        } else {
            return success(errorList, 400);
        }
    }

    @GetMapping(value = "/parent/mentalfile/list")
    @ApiOperation(value = "家长心理档案信息分页")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "姓名", dataType = "String"),
        @ApiImplicitParam(name = "grade", value = "年纪code", paramType = "query", required = true, dataType = "string"),
        @ApiImplicitParam(name = "clazzId", value = "班级id", paramType = "query", required = true, dataType = "long")})
    public CommonResult<PageResult<ParentBaseRespVO>> listBaseInfo(@ApiIgnore @RequestParam Map<String, Object> params) {
        return success(parentService.listBaseInfo(params));
    }

    @PostMapping(value = "/parent/getParentUserIdByParams")
    @ApiOperation(value = "根据查询条件查询家长用户id")
    public CommonResult<List<Long>> getParentUserIdByParams(@RequestBody Map<String, Object> params) {
        return success(parentService.getParentUserIdByParams(params));
    }

    @GetMapping(value = "/parent/findParentInfo")
    @ApiOperation(value = "根据用户id查询家长基本信息及子女信息")
    public CommonResult<com.cnasoft.health.common.dto.ParentStudentDTO> findParentInfo(@RequestParam Long userId) {
        return success(parentService.findParentInfo(userId));
    }

    @PostMapping(value = "/parent/findParentList")
    @ApiOperation(value = "根据用户id列表查询家长基本信息及子女姓名")
    public CommonResult<List<com.cnasoft.health.common.dto.ParentDTO>> findParentList(@RequestBody Set<Long> userIds) {
        return success(parentService.findParentList(userIds));
    }
}
