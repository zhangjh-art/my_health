package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.EarlyWarningStatusUpdateReqVO;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.DynamicWarningRespVO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicDTO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicReqVO;
import com.cnasoft.health.userservice.model.UserDynamic;

import java.util.Map;

public interface IUserDynamicService {

    /**
     * 获取用户心情动态
     *
     * @param params
     * @return
     */
    PageResult<UserDynamicDTO> getUserDynamicPage(Map<String, Object> params);

    /**
     * 保存用户心情信息
     * 使用事务消息后，这里接口调用，只做校验和发送消息到消息队列，不入数据库
     *
     * @param userDynamic
     */
    void saveUserDynamicInfo(UserDynamicReqVO userDynamic);

    /**
     * rocket 事务消息执行本地事务，业务数据入库
     * @param userDynamic 业务数据
     * @param transactionId 消息唯一标识
     */
    void saveUserDynamicInfo(UserDynamic userDynamic,String transactionId);

    /**
     * 删除用户心情，做一个状态上的修改并非真的删除
     *
     * @param dynamicId
     */
    void deleteDynamic(Long dynamicId);

    /**
     * 置顶动态--修改排序
     *
     * @param dynamicId
     */
    void updateDynamic(Long dynamicId, Integer sort);

    /**
     * 校心理老师查询动态预警
     *
     * @param params type: 1:学生；2：家长；3：老师
     * @return
     */
    PageResult<DynamicWarningRespVO> getSchoolDynamicWarning(Map<String, Object> params);

    /**
     * 校心理老师处理动态预警
     *
     * @param vo
     */
    void dealSchoolDynamicWarning(EarlyWarningStatusUpdateReqVO vo);

    /**
     * 区域心理教研员处理动态预警
     *
     * @param vo
     */
    void dealAreaDynamicWarning(EarlyWarningStatusUpdateReqVO vo);

    /**
     * 区域心理教研员查询动态预警列表
     *
     * @param params
     * @return
     */
    PageResult<DynamicWarningRespVO> getAreaDynamicWarning(Map<String, Object> params);

    /**
     * 根据id查询
     *
     * @param id 心情id
     * @return
     */
    UserDynamicDTO selectUserDynamic(Long id);
}
