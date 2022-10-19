package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.constant.ServiceNameConstants;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.NewReservationRespVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 预约feign接口
 */
@FeignClient(name = ServiceNameConstants.USER_SERVICE, decode404 = true)
public interface NewReservationFeign {
    /**
     * 根据年月获取预约统计数据
     *
     * @param year  年份
     * @param month 月份
     * @return SchoolDTO
     */
    @GetMapping(value = "/newReservation/statistical")
    CommonResult<Map<String, List<Map<String, Object>>>> getStatisticalData(@RequestParam("year") Integer year,
        @RequestParam("month") Integer month);

    /**
     * 查询心理老师待确认预约
     */
    @GetMapping(value = "/newReservation/toConfirmed")
    CommonResult<PageResult<NewReservationRespVO>> getToConfirmedTask();

    /**
     * 查询用户已确认预约
     */
    @GetMapping(value = "/newReservation/confirmed")
    CommonResult<PageResult<NewReservationRespVO>> getConfirmedTask();
}
