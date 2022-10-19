package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.constant.ServiceNameConstants;
import com.cnasoft.health.common.vo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Created by lgf on 2022/4/26.
 */
@FeignClient(name = ServiceNameConstants.TASK_SERVICE, decode404 = true)
public interface TaskFeign {

    /**
     * 重新为指定区域或者学校的任务确认承接人
     *
     * @param schoolId 学校id
     * @param areaCode 区域code
     * @param userId   承接人用户id
     * @return null
     */
    @GetMapping("/task/reconfirmTaskHandler")
    CommonResult<Object> reconfirmTaskHandler(@RequestParam(value = "schoolId", required = false) Long schoolId,
        @RequestParam(value = "areaCode", required = false) Integer areaCode, @RequestParam(value = "userId", required = true) Long userId);

    /**
     * 修改量表启用状态
     */
    @PutMapping("/gauge/info/updateEnabled")
    CommonResult<Object> updateEnabled(@RequestParam("id") Long id, @RequestParam("enabled") Boolean enabled);

    @GetMapping(value = "/gauge/getGaugeIdByQuery")
    CommonResult<List<Long>> getGaugeIdByQuery(@RequestParam(value = "query", required = false) String query, @RequestParam(value = "gaugeType", required = false) String gaugeType,
        @RequestParam(value = "isDeleted", required = false) Integer isDeleted);
}
