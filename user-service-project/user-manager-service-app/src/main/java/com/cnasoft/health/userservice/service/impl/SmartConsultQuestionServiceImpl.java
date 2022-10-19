package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.feign.dto.ConsultationRecordReqVO;
import com.cnasoft.health.userservice.feign.dto.SmartConsultQuestionResVO;
import com.cnasoft.health.userservice.mapper.SmartConsultQuestionMapper;
import com.cnasoft.health.userservice.model.SmartConsultQuestion;
import com.cnasoft.health.userservice.service.IConsultationRecordService;
import com.cnasoft.health.userservice.service.SmartConsultQuestionService;
import com.cnasoft.health.userservice.util.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SmartConsultQuestionServiceImpl extends SuperServiceImpl<SmartConsultQuestionMapper, SmartConsultQuestion>
    implements SmartConsultQuestionService {

    @Resource
    private IConsultationRecordService consultationRecordService;

    @Override
    public List<SmartConsultQuestionResVO> findList() {
        List<SmartConsultQuestionResVO> list = baseMapper.findList();
        list.forEach(e -> {
            if (CollectionUtils.isEmpty(e.getQuestionInfos())) {
                List<SmartConsultQuestionResVO> questionRes = baseMapper.findSubList(e.getType());
                e.setQuestionRes(questionRes);
            }
        });
        return list;
    }

    @Override
    public String getAnswerById(Long id) {
        SmartConsultQuestion question = baseMapper.selectById(id);
        ConsultationRecordReqVO recordVO = new ConsultationRecordReqVO();
        recordVO.setSource(0);
        recordVO.setSourceId(id);
        recordVO.setUserId(UserUtil.getCurrentUser().getId());
        recordVO.setConsultTypes(question.getType());
        consultationRecordService.create(recordVO);

        return question.getAnswer();
    }
}
