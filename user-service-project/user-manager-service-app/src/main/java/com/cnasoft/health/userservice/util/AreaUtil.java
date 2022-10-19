package com.cnasoft.health.userservice.util;

import com.cnasoft.health.common.dto.AreaDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import org.springframework.stereotype.Component;

/**
 * @author lgf
 * @date 2022/3/22
 */
@Component
public class AreaUtil {

    public AreaDTO convert(SysAreaDTO dto) {
        AreaDTO vo = new AreaDTO();
        vo.setCode(dto.getCode());
        vo.setType(dto.getType());
        vo.setName(dto.getName());
        return vo;
    }

    public SysAreaDTO getArea(final Integer code) {
        return RedisUtils.getAreaByCache(code);
    }
}
