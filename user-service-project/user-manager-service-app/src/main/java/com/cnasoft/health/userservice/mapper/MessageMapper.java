package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.Message;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Created by lgf on 2022/3/29.
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Component
public interface MessageMapper extends SuperMapper<Message> {
    List<Message> findList(Page<Message> page, @Param("u") Map<String, Object> params);
}
