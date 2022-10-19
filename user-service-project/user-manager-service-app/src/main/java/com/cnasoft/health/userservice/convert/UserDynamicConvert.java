package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.userservice.feign.dto.UserDynamicReqVO;
import com.cnasoft.health.userservice.model.UserDynamic;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDynamicConvert {

    UserDynamicConvert INSTANCE = Mappers.getMapper(UserDynamicConvert.class);

    /**
     * 转化为用户动态实体对象
     *
     * @param userDynamicReqVO 用户动态对象
     * @return 用户动态基本信息
     */
    UserDynamic convertUserDynamicVO(UserDynamicReqVO userDynamicReqVO);
}
