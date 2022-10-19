package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.constant.ServiceNameConstants;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.vo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测评模块学校feign接口
 *
 * @author zcb
 */
@FeignClient(name = ServiceNameConstants.USER_SERVICE, decode404 = true)
public interface SchoolFeignClient {

    /**
     * 根据学校id获取学校信息
     *
     * @param id 学校id
     * @return SchoolDTO
     */
    @GetMapping(value = "/school/detail")
    CommonResult<SchoolDTO> getSchoolById(@RequestParam("id") Long id);

    /**
     * 根据学校id列表获取学校信息列表
     *
     * @param ids 学校id列表
     * @return SchoolDTO
     */
    @GetMapping(value = "/school/list/server")
    CommonResult<List<SchoolDTO>> getSchoolListByIds(@RequestBody Set<Long> ids);

    /**
     * 根据学校id和班级id列表获取对应的学生的用户id列表
     *
     * @param schoolIds 学校id集合
     * @return 学生的用户id列表
     */
    @GetMapping(value = "/school/list/ids")
    CommonResult<List<CommonDTO>> getSchoolByIds(@RequestParam("schoolIds") Set<Long> schoolIds);

    /**
     * 获取测试管理员管理的学校数据
     *
     * @return SchoolDTO
     */
    @GetMapping(value = "/user/test/manage/school")
    CommonResult<List<SchoolDTO>> findSchoolListByTestManager();

    /**
     * 获取学校数据增量统计(当年)
     *
     * @return List<Map<String, Object>>
     */
    @GetMapping(value = "/school/statistics")
    CommonResult<List<Map<String, Object>>> getSchoolStatistics();
}
