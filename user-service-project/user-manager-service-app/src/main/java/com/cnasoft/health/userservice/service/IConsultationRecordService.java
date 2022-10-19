package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.ConsultationRecordReqVO;
import com.cnasoft.health.userservice.feign.dto.ConsultationRecordRespVO;
import com.cnasoft.health.userservice.model.ConsultationRecord;

import java.util.Map;

public interface IConsultationRecordService extends ISuperService<ConsultationRecord> {
    /**
     * 新增咨询记录
     *
     * @param vo 请求数据
     */
    void create(ConsultationRecordReqVO vo);

    /**
     * 获取咨询记录列表
     *
     * @return 咨询记录列表
     */
    PageResult<ConsultationRecordRespVO> listByUserId(Map<String, Object> params);
}
