package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.userservice.feign.dto.SensitiveWordCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordDTO;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordUpdateReqVO;
import com.cnasoft.health.userservice.model.SensitiveWord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author ganghe
 */
@Mapper
public interface SensitiveWordConvert {
    SensitiveWordConvert INSTANCE = Mappers.getMapper(SensitiveWordConvert.class);

    /**
     * DTO转换为实体对象
     *
     * @param createReqVO
     * @return
     */
    SensitiveWord convertVO(SensitiveWordCreateReqVO createReqVO);

    /**
     * DTO转换为实体对象
     *
     * @param updateReqVO
     * @return
     */
    SensitiveWord convertVO(SensitiveWordUpdateReqVO updateReqVO);

    /**
     * 实体集合转换为数据集合
     *
     * @param sensitiveWords
     * @return
     */
    List<SensitiveWordDTO> convertList(List<SensitiveWord> sensitiveWords);
}
