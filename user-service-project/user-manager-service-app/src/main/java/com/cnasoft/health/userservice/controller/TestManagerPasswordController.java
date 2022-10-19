package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.TestManagerPasswordDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.TestManagerPasswordReqVO;
import com.cnasoft.health.userservice.service.ITestManagerPasswordService;
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

import static com.cnasoft.health.common.vo.CommonResult.error;
import static com.cnasoft.health.common.vo.CommonResult.success;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.COMMON_MESSAGE;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.PASSWORD_ERROR;

/**
 * 测试管理员密码管理接口
 *
 * @author ganghe
 * @date 2022/7/15 10:16
 **/
@Slf4j
@RestController
@Api(tags = "测试管理员密码管理API")
public class TestManagerPasswordController {
    @Resource
    private ITestManagerPasswordService testManagerPasswordService;

    @GetMapping(value = "/test/manage/password/list")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "schoolId", value = "学校id", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "applicationScene", value = "应用场景", required = true, dataType = "Integer")})
    @ApiOperation(value = "测试管理员密码管理列表")
    public CommonResult<PageResult<TestManagerPasswordDTO>> list(@RequestParam Map<String, Object> params) {
        return success(testManagerPasswordService.findListByPage(params));
    }

    @PostMapping(value = "/test/manage/password/info")
    @ApiOperation(value = "新增测试管理员密码")
    public CommonResult<Object> savePassword(@RequestBody @Validated TestManagerPasswordReqVO reqVO) {
        Boolean result = testManagerPasswordService.savePassword(reqVO);
        return result.equals(Boolean.TRUE) ? success() : error(COMMON_MESSAGE.getMessage());
    }

    @PutMapping(value = "/test/manage/password/info")
    @ApiOperation(value = "修改测试管理员密码")
    public CommonResult<Object> updatePassword(@RequestBody @Validated(TestManagerPasswordReqVO.Update.class) TestManagerPasswordReqVO reqVO) {
        Boolean result = testManagerPasswordService.updatePassword(reqVO);
        return result.equals(Boolean.TRUE) ? success() : error(COMMON_MESSAGE.getMessage());
    }

    @DeleteMapping("/test/manage/password/info")
    @ApiOperation(value = "删除测试管理员密码")
    public CommonResult<List<BatchOperationTipDTO>> deleteSchoolTeacher(@RequestParam Set<Long> ids) {
        return success(testManagerPasswordService.deletePassword(ids));
    }

    @PutMapping(value = "/test/manage/password/check")
    @ApiOperation(value = "根据应用场景校验密码")
    public CommonResult<Object> checkPassword(@RequestBody @Validated(TestManagerPasswordReqVO.Check.class) TestManagerPasswordReqVO reqVO) {
        Boolean result = testManagerPasswordService.checkPassword(reqVO);
        return result.equals(Boolean.TRUE) ? success() : error(PASSWORD_ERROR.getMessage());
    }

    @PutMapping(value = "/test/manage/password/check/logOut")
    @ApiOperation(value = "校验退出系统密码")
    public CommonResult<Object> checkPasswordWithLogOut(@RequestBody @Validated(TestManagerPasswordReqVO.CheckBackHome.class) TestManagerPasswordReqVO reqVO) {
        Boolean result = testManagerPasswordService.checkPasswordWithLogOut(reqVO);
        return result.equals(Boolean.TRUE) ? success() : error(PASSWORD_ERROR.getMessage());
    }
}
