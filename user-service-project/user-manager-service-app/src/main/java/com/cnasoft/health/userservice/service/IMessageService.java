package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.MessageResVO;
import com.cnasoft.health.userservice.model.Message;

import java.util.List;
import java.util.Map;

/**
 * @Created by lgf on 2022/3/29.
 */
public interface IMessageService extends ISuperService<Message> {
    PageResult<MessageResVO> findList(Map<String, Object> params);

    void batchRead(List<Long> messageIds);
}
