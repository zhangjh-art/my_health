package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.convert.SensitiveWordConvert;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordDTO;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordUpdateReqVO;
import com.cnasoft.health.userservice.model.SensitiveWord;
import com.cnasoft.health.userservice.service.ISensitiveWordService;
import io.swagger.annotations.Api;
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
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.DELETE_FAILED;
import static com.cnasoft.health.common.vo.CommonResult.error;
import static com.cnasoft.health.common.vo.CommonResult.success;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:53
 **/
@Slf4j
@RestController
@Api(tags = "敏感词库API")
public class SensitiveWordController {

    @Resource
    private ISensitiveWordService sensitiveWordService;

    @GetMapping("/sensitive/list")
    @ApiOperation(value = "获取敏感词库列表")
    public CommonResult<PageResult<SensitiveWordDTO>> getAreaList(@RequestParam Map<String, Object> params) {
        return CommonResult.success(sensitiveWordService.selectPage(params));
    }

    @PostMapping(value = "/sensitive/info")
    @ApiOperation(value = "新增敏感词")
    public CommonResult<SensitiveWord> save(@RequestBody @Validated SensitiveWordCreateReqVO createReqVO) {
        sensitiveWordService.save(SensitiveWordConvert.INSTANCE.convertVO(createReqVO));
        return success();
    }

    @PutMapping(value = "/sensitive/info")
    @ApiOperation(value = "更新敏感词")
    public CommonResult<SensitiveWord> update(@RequestBody @Validated SensitiveWordUpdateReqVO updateReqVO) {
        sensitiveWordService.updateById(SensitiveWordConvert.INSTANCE.convertVO(updateReqVO));
        return success();
    }

    @DeleteMapping("/sensitive/info")
    @ApiOperation(value = "删除指定敏感词")
    public CommonResult<SensitiveWord> delete(@RequestParam Set<Long> ids) {
        boolean result = sensitiveWordService.removeByIds(ids);
        return result ? success() : error(DELETE_FAILED.getMessage());
    }
}
