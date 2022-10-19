package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SensitiveWordConvert;
import com.cnasoft.health.userservice.feign.dto.SensitiveWordDTO;
import com.cnasoft.health.userservice.mapper.SensitiveWordMapper;
import com.cnasoft.health.userservice.model.SensitiveWord;
import com.cnasoft.health.userservice.service.ISensitiveWordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:51
 **/
@Service
public class SensitiveWordServiceImpl extends SuperServiceImpl<SensitiveWordMapper, SensitiveWord> implements ISensitiveWordService {

    @Override
    public PageResult<SensitiveWordDTO> selectPage(Map<String, Object> params) {
        Page<SensitiveWord> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        List<SensitiveWord> sensitiveWords = baseMapper.findList(page, params);

        List<SensitiveWordDTO> list = SensitiveWordConvert.INSTANCE.convertList(sensitiveWords);

        return PageResult.<SensitiveWordDTO>builder().data(list).count(page.getTotal()).build();
    }
}
