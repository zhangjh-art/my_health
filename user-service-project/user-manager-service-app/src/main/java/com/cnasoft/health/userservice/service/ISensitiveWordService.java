package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordDTO;
import com.cnasoft.health.userservice.model.SensitiveWord;

import java.util.Map;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:50
 **/
public interface ISensitiveWordService extends ISuperService<SensitiveWord> {
    /**
     * 分页查询数据
     *
     * @param params
     * @return
     */
    PageResult<SensitiveWordDTO> selectPage(Map<String, Object> params);
}
