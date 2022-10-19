package com.cnasoft.health.userservice.controller;

import cn.hutool.core.lang.Assert;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveRecord;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.DictNameReqVO;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.service.ISysDictService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.success;

/**
 * @author lqz
 */
@Slf4j
@RestController
@Api(tags = "数据字典API")
public class SysDictController {
    @Resource
    private ISysDictService sysDictService;

    @PostMapping("/dict/data/listPage")
    @ApiOperation(value = "我的工作->字典管理")
    public CommonResult<PageResult<SysDictDTO>> listPage(@RequestBody DictNameReqVO query) {
        return success(sysDictService.listPage(query));
    }

    @GetMapping("/dict/data/list")
    @ApiOperation(value = "获取全部数据字典列表")
    public CommonResult<List<SysDictDTO>> getDictDataList(@RequestParam String type) {
        Assert.notBlank(type, "字典类型不能为空");

        return success(sysDictService.listDictDataByType(type, true, ApproveStatus.APPROVED));
    }

    @GetMapping("/dict/data/types/list")
    @ApiOperation(value = "根据types获取全部数据字典列表")
    public CommonResult<Map<String, List<SysDictDTO>>> getDictDataMap(@RequestParam List<String> types) {
        return success(sysDictService.listDictDataByTypes(types, true, ApproveStatus.APPROVED));
    }

    @GetMapping("/dict/data/listNoCache")
    @ApiOperation(value = "获取全部数据字典列表")
    public CommonResult<List<SysDictDTO>> getDictDataListNoCache(@RequestParam String type) {
        Assert.notBlank(type, "字典类型不能为空");
        return success(sysDictService.listDictDataByType(type, false, null));
    }

    @PostMapping("/dict/data/info/server")
    @ApiOperation(value = "新增字典数据,其他服务模块使用")
    public CommonResult<String> saveDictDataByServer(@RequestBody @Validated SysDictDTO dictDTO) {
        SysDictData sysDictData = sysDictService.saveDictDataOtherModule(dictDTO);
        return success(sysDictData.getDictValue());
    }

    @PutMapping("/dict/data/info/server")
    @ApiOperation(value = "修改字典数据审核状态,其他服务模块使用")
    public CommonResult<Object> updateDictDataByServer(@RequestBody Set<String> gaugeTypeSet, @RequestParam Integer approveStatus) {
        sysDictService.updateDictDataApproveStatus(gaugeTypeSet, approveStatus);
        return success();
    }

    @PostMapping("/dict/data/info")
    @ApiOperation(value = "新增字典数据")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_DICT_DATA)
    public CommonResult<SysDictData> saveDictData(@RequestBody @Validated SysDictDTO dictDTO) {
        return success(sysDictService.saveDictData(dictDTO));
    }

    @PutMapping("/dict/data/info")
    @ApiOperation(value = "修改字典数据")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_DICT_DATA)
    public CommonResult<Object> updateDictData(@RequestBody @Validated SysDictDTO dictDTO) {
        sysDictService.updateDictData(dictDTO);
        return success();
    }

    @DeleteMapping("/dict/data/delete")
    @ApiOperation(value = "删除字典数据")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_DICT_DATA)
    public CommonResult<List<BatchOperationTipDTO>> deleteDictData(@RequestParam Set<Long> ids) {
        return success(sysDictService.deleteDictData(ids));
    }

    @GetMapping("/dict/h5BannerList")
    @ApiOperation(value = "获取h5端轮播图列表")
    public CommonResult<List<String>> getH5BannerList() {
        return success(sysDictService.getH5BannerList());
    }
}
