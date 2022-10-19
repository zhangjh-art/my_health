package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.TestManagerPasswordDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.TestManagerPasswordReqVO;
import com.cnasoft.health.userservice.model.TestManagerPassword;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试管理员密码管理
 *
 * @author ganghe
 */
public interface ITestManagerPasswordService extends ISuperService<TestManagerPassword> {
    /**
     * 测试管理员密码管理列表
     *
     * @param params 查询条件
     * @return 分页数据
     */
    PageResult<TestManagerPasswordDTO> findListByPage(Map<String, Object> params);

    /**
     * 新增测试管理员密码
     *
     * @param reqVO 请求数据
     * @return Boolean
     */
    Boolean savePassword(TestManagerPasswordReqVO reqVO);

    /**
     * 修改测试管理员密码
     *
     * @param reqVO 请求数据
     * @return Boolean
     */
    Boolean updatePassword(TestManagerPasswordReqVO reqVO);

    /**
     * 删除测试管理员密码
     *
     * @param ids 测试管理员密码ID列表
     * @return 受影响的行数
     */
    List<BatchOperationTipDTO> deletePassword(Set<Long> ids);

    /**
     * 根据应用场景校验密码
     * @param reqVO 请求数据
     * @return Boolean
     */
    Boolean checkPassword(TestManagerPasswordReqVO reqVO);

    /**
     * 校验返回首页密码
     * @param reqVO 请求数据
     * @return Boolean
     */
    Boolean checkPasswordWithLogOut(TestManagerPasswordReqVO reqVO);
}
