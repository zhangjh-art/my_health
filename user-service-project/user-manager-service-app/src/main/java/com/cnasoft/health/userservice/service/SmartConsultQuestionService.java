package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.feign.dto.SmartConsultQuestionResVO;
import com.cnasoft.health.userservice.model.SmartConsultQuestion;

import java.util.List;

/**
 * 咨询小精灵问题
 */
public interface SmartConsultQuestionService extends ISuperService<SmartConsultQuestion> {

    /**
     * 查询咨询小精灵问题列表
     *
     * @return
     */
    List<SmartConsultQuestionResVO> findList();

    /**
     * 查询咨询小精灵问题答案, 并新增咨询记录
     *
     * @return
     */
    String getAnswerById(Long id);
}
