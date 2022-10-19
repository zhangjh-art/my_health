package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.SmartConsultQuestionResVO;
import com.cnasoft.health.userservice.service.SmartConsultQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 咨询小精灵
 */
@Slf4j
@RestController
@Api(tags = "咨询小精灵API")
public class SmartConsultQuestionController {

    @Resource
    private SmartConsultQuestionService smartConsultQuestionService;

    @GetMapping("/smartConsultQuestion/list")
    @ApiOperation(value = "咨询小精灵问题列表")
    public CommonResult<List<SmartConsultQuestionResVO>> getQuestionList() {
        return CommonResult.success(smartConsultQuestionService.findList());
    }

    @GetMapping("/smartConsultQuestion/get")
    @ApiOperation(value = "咨询小精灵问题答案")
    public CommonResult<String> getQuestion(@RequestParam("id") Long id) {
        return CommonResult.success(smartConsultQuestionService.getAnswerById(id));
    }
}
