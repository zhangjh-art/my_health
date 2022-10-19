package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.EarlyWarningStatusUpdateReqVO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.DynamicWarningRespVO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicDTO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicReqVO;
import com.cnasoft.health.userservice.model.UserDynamic;
import com.cnasoft.health.userservice.service.IUserDynamicService;
import com.cnasoft.health.userservice.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

import static com.cnasoft.health.common.vo.CommonResult.success;

/**
 * @author Administrator
 */
@Slf4j
@RestController
@RequestMapping("/userMood")
@Api(tags = "我的应用")
public class UserDynamicController {

    @Resource
    IUserDynamicService userDynamicService;

    @PostMapping(value = "/userMoodList")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer")
            , @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"),})
    @ApiOperation(value = "我的心情列表")
    public CommonResult<PageResult<UserDynamicDTO>> list(@RequestParam Map<String, Object> params) {
        Long userId = UserUtil.getUserId();
        params.put("userId", userId);
        return success(userDynamicService.getUserDynamicPage(params));
    }

    @PostMapping("/addUserMood")
    @ApiOperation(value = "新增心情")
    public CommonResult<Object> saveUserDynamic(@RequestBody @Validated UserDynamicReqVO info) {
        userDynamicService.saveUserDynamicInfo(info);
        return success();
    }

    @PostMapping("/detail")
    @ApiOperation(value = "心情详情")
    public CommonResult<UserDynamicDTO> getUserDynamic(@RequestParam Long id) {
        UserDynamicDTO userDynamicDTO = userDynamicService.selectUserDynamic(id);
        return success(userDynamicDTO);
    }

    @PostMapping("/deleteUserMood")
    @ApiOperation(value = "删除心情")
    public CommonResult<Object> deleteUserDynamic(@RequestBody UserDynamic info) {
        userDynamicService.deleteDynamic(info.getId());
        return success();
    }

    @PostMapping("/toppingUserMood")
    @ApiOperation(value = "心情置顶/取消置顶")
    public CommonResult<Object> toppingUserDynamic(@RequestBody UserDynamic info) {
        userDynamicService.updateDynamic(info.getId(), info.getSort());
        return success();
    }

    @PostMapping("/getSchoolDynamicWarning")
    @ApiOperation(value = "校心理老师查询动态预警列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"), @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "type", value = "查询类型，1:学生；2：家长；3：老师", dataType = "String"),})
    public CommonResult<PageResult<DynamicWarningRespVO>> getSchoolDynamicWarning(@RequestBody Map<String, Object> params) {
        return success(userDynamicService.getSchoolDynamicWarning(params));
    }

    @PostMapping("/dealSchoolDynamicWarning")
    @ApiOperation(value = "校心理老师处置动态预警")
    public CommonResult<PageResult<DynamicWarningRespVO>> dealSchoolDynamicWarning(@RequestBody @Validated EarlyWarningStatusUpdateReqVO vo) {
        userDynamicService.dealSchoolDynamicWarning(vo);
        return success();
    }

    @PostMapping("/getAreaDynamicWarning")
    @ApiOperation(value = "区域心理教研员查询动态预警列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"), @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer")})
    public CommonResult<PageResult<DynamicWarningRespVO>> getAreaDynamicWarning(@RequestBody Map<String, Object> params) {
        return success(userDynamicService.getAreaDynamicWarning(params));
    }

    @PostMapping("/dealAreaDynamicWarning")
    @ApiOperation(value = "区域心理教研员处置动态预警")
    public CommonResult<PageResult<DynamicWarningRespVO>> dealAreaDynamicWarning(@RequestBody @Validated EarlyWarningStatusUpdateReqVO vo) {
        userDynamicService.dealAreaDynamicWarning(vo);
        return success();
    }
}
