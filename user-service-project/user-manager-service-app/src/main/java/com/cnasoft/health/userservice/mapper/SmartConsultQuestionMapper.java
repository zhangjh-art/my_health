package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.SmartConsultQuestionResVO;
import com.cnasoft.health.userservice.model.SmartConsultQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:49
 **/
public interface SmartConsultQuestionMapper extends SuperMapper<SmartConsultQuestion> {

    /**
     * 查询小精灵只有一级分类的咨询问题
     *
     * @return
     */
    List<SmartConsultQuestionResVO> findList();

    /**
     * 查询小精灵有二级分类的咨询问题
     *
     * @return
     */
    List<SmartConsultQuestionResVO> findSubList(@Param("type") String type);
}
