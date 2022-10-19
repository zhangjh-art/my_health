package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.FeedbackVO;
import com.cnasoft.health.userservice.feign.dto.MessageResVO;
import com.cnasoft.health.userservice.service.IMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Created by lgf on 2022/4/6.
 */
@Slf4j
@RestController
@RequestMapping("/message")
@Api(tags = "消息通知API")
public class MessageController {
    @Resource
    private IMessageService messageService;

    @GetMapping("/getList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer")
    })
    public CommonResult<PageResult<MessageResVO>> getMessageList(@RequestParam Map<String, Object> params) {
        return CommonResult.success(messageService.findList(params));
    }

    @PostMapping("/batchRead")
    public CommonResult<Object> batchRead(@RequestBody List<Long> messageIds) {
        messageService.batchRead(messageIds);
        return CommonResult.success();
    }

    @PostMapping("/feedback")
    @ApiOperation("h5意见反馈")
    public CommonResult<Object> feedback(@RequestBody @Valid FeedbackVO vo) {
        log.info(JsonUtils.writeValueAsString(vo));
        return CommonResult.success();
    }
}
