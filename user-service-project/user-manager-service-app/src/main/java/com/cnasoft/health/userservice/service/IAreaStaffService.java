package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.AreaStaffDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.AreaStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.model.AreaStaff;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 区域职员
 *
 * @author lgf
 * @date 2022/3/29
 */
public interface IAreaStaffService extends ISuperService<AreaStaff> {
    /**
     * 分页查询区域职员列表
     *
     * @param params 查询条件
     * @return 分页数据
     */
    PageResult<AreaStaffRespVO> findList(Map<String, Object> params);

    /**
     * 保存区域职员
     *
     * @param staffReqVO 区域职员数据
     * @throws Exception 异常
     */
    AreaStaff saveAreaStaff(AreaStaffReqVO staffReqVO) throws Exception;

    /**
     * 编辑区域职员
     *
     * @param staffReqVO 区域职员数据
     * @throws Exception 异常
     */
    void updateAreaStaff(AreaStaffReqVO staffReqVO) throws Exception;

    /**
     * 删除区域职员
     *
     * @param ids id列表
     * @return 统一结果
     */
    List<BatchOperationTipDTO> deleteAreaStaff(Set<Long> ids);

    /**
     * 获取区域职员详情
     *
     * @param userId 用户id
     * @return 区域职员信息
     */
    AreaStaffRespVO findByUserId(Long userId);

    /**
     * 修改当前登录用户信息
     *
     * @param staffReqVO 修改后的数据
     */
    void updateCurrentAreaStaff(AreaStaffReqVO staffReqVO) throws Exception;

    /**
     * 根据区域编码删除
     *
     * @param areaCode 区域编码
     */
    void deleteByAreaCode(Integer areaCode);

    /**
     * 区域职员下拉列表
     *
     * @param areaCode 区域编码
     * @param userName 职员名称
     * @return 区域职员下拉列表
     */
    List<Map<String, Object>> getSelectList(Integer areaCode, String userName);

    /**
     * 获取区域职员心理档案列表
     *
     * @param params
     * @return
     */
    PageResult<StaffMentalFileVO> listMentalFile(Map<String, Object> params);

    /**
     * 获取区域职员详情，包含区域心理教师
     *
     * @param userId 用户id
     * @return 区域职员信息
     */
    UserRespVO findUnionUser(Long userId);

    /**
     * 获取区域职员信息
     *
     * @param userId 用户id
     * @return 区域职员信息
     */
    AreaStaffDTO findAreaStaffInfo(Long userId);
}
