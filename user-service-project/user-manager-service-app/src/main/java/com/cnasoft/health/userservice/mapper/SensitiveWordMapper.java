package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.SensitiveWord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:49
 **/
public interface SensitiveWordMapper extends SuperMapper<SensitiveWord> {

    /**
     * 分页查询敏感词库
     *
     * @param page
     * @param params
     * @return
     */
    List<SensitiveWord> findList(Page<SensitiveWord> page, @Param("p") Map<String, Object> params);
}
