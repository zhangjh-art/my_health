package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.TestManagerPasswordDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.TestManagerPassword;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试管理员密码管理
 *
 * @author ganghe
 */
public interface TestManagerPasswordMapper extends SuperMapper<TestManagerPassword> {
    /**
     * 分页查询测试管理员密码列表
     *
     * @param page   分页对象
     * @param params 查询条件
     * @return 用户账号列表
     */
    List<TestManagerPasswordDTO> findListByPage(Page<TestManagerPasswordDTO> page, @Param("params") Map<String, Object> params);

    /**
     * 查询指定用户指定学校指定场景是否已设置密码
     *
     * @param userId           用户id
     * @param schoolId         学校id
     * @param applicationScene 场景
     * @return id
     */
    Long findExist(@Param("userId") Long userId, @Param("schoolId") Long schoolId, @Param("applicationScene") Integer applicationScene);

    /**
     * 修改测试管理员密码
     *
     * @param testManagerPassword 测试管理员密码数据
     * @return 受影响的行数
     */
    int update(TestManagerPassword testManagerPassword);

    /**
     * 查询指定用户指定学校指定场景设置的密码
     *
     * @param userId           测试管理员用户id
     * @param schoolId         学校id
     * @param applicationScene 场景
     * @param key              key
     * @return 密码
     */
    String findPassword(@Param("userId") Long userId, @Param("schoolId") Long schoolId, @Param("applicationScene") Integer applicationScene, @Param("key") String key);
}
