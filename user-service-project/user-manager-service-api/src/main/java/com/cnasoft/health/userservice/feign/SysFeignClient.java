package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.constant.ServiceNameConstants;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.vo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * @Created by lgf on 2022/3/22.
 */
@FeignClient(name = ServiceNameConstants.USER_SERVICE, decode404 = true)
public interface SysFeignClient {

    /**
     * 查询区域列表信息
     *
     * @param type 区域类型 0为省/直辖市/自治区 1为市 2为区/县
     * @return List<SysAreaDTO>
     */
    @GetMapping(value = "/area/list")
    CommonResult<List<SysAreaDTO>> getAreaList(Integer type);

    /**
     * 新增字典数据
     *
     * @param dictDTO
     * @return
     */
    @PostMapping("/dict/data/info/server")
    CommonResult<String> saveDictData(@RequestBody @Validated SysDictDTO dictDTO);

    /**
     * 审核量表时处理自定义量表类型的审核状态
     *
     * @param gaugeTypeSet  量表类型集合
     * @param approveStatus 审核状态
     * @return
     */
    @PutMapping("/dict/data/info/server")
    CommonResult<String> updateDictDataByServer(@RequestBody Set<String> gaugeTypeSet, @RequestParam("approveStatus") Integer approveStatus);
}
