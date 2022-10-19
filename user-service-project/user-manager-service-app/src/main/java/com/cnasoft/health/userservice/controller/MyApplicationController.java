package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.StudentApplicationDto;
import com.cnasoft.health.userservice.model.StudentApplication;
import com.cnasoft.health.userservice.service.IStudentApplicationService;
import com.cnasoft.health.userservice.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cnasoft.health.common.vo.CommonResult.success;

@Slf4j
@RestController
@RequestMapping("/myApplication")
@Api(tags = "我的应用")
public class MyApplicationController {

    @Autowired
    IStudentApplicationService iStudentApplicationService;

    @PostMapping(value = "/myApplication/list")
    @ApiOperation(value = "家长列表")
    public CommonResult<List<StudentApplicationDto>> list() {
        Long userId = UserUtil.getUserId();
        return success(iStudentApplicationService.queryStudentApplicationList(userId));
    }
}
