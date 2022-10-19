package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.userservice.feign.dto.TestManagerPasswordReqVO;
import com.cnasoft.health.userservice.model.TestManagerPassword;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 测试管理员密码管理转换器
 *
 * @author ganghe
 * 2022/7/15
 */
@Mapper
public interface TestManagerPasswordConvert {
    TestManagerPasswordConvert INSTANCE = Mappers.getMapper(TestManagerPasswordConvert.class);

    /**
     * 测试管理员密码管理请求数据转换为实体类
     *
     * @param reqVO 测试管理员密码管理请求对象
     * @return 用户对象
     */
    TestManagerPassword convert(TestManagerPasswordReqVO reqVO);
}
