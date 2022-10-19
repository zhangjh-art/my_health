package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.MessageResVO;
import com.cnasoft.health.userservice.mapper.MessageMapper;
import com.cnasoft.health.userservice.model.Message;
import com.cnasoft.health.userservice.service.IMessageService;
import com.cnasoft.health.userservice.service.ISysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 站内信实现
 *
 * @author lgf
 */
@Service
public class MessageServiceImpl
        extends SuperServiceImpl<MessageMapper, Message>
        implements IMessageService {

    @Resource
    private ISysUserService userService;

    @Override
    public PageResult<MessageResVO> findList(Map<String, Object> params) {
        Long currentUserId = SysUserUtil.getHeaderUserId();
        params.put("userId", currentUserId);

        Page<Message> page = new Page<>(MapUtil.getInt(params, "pageNum", 1), MapUtil.getInt(params, "pageSize", 10));

        List<Message> records = baseMapper.findList(page, params);
        long total = page.getTotal();
        List<MessageResVO> resVOList = new ArrayList<>(records.size());
        records.forEach(record -> {
            MessageResVO resVO = new MessageResVO();
            resVO.setId(record.getId().toString());
            resVO.setUserId(record.getUserId().toString());
            resVO.setType(record.getType());
            resVO.setContent(record.getContent());
            resVO.setParams(record.getParams());
            resVO.setHasRead(record.isHasRead());
            resVO.setCreateBy(record.getCreateBy().toString());
            if (record.getCreateBy() != 0L) {
                SysUserDTO user = userService.findByUserId(record.getCreateBy(), false);
                if (Objects.nonNull(user)) {
                    resVO.setCreateByUsername(user.getName());
                }
            }
            resVO.setCreateTime(record.getCreateTime());
            resVOList.add(resVO);
        });

        return PageResult.<MessageResVO>builder().data(resVOList).count(total).build();
    }

    @Override
    public void batchRead(List<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return;
        }

        Long userId = SysUserUtil.getHeaderUserId();
        List<Message> models = new ArrayList<>(messageIds.size());
        messageIds.forEach(id -> {
            // 只有当前用户和消息的用户id一致才进行更新
            Message model = baseMapper.selectOne(
                    new QueryWrapper<Message>()
                            .eq("id", id)
                            .eq("is_deleted", 0)
                            .eq("has_read", 0)
                            .eq("user_id", userId));
            if (Objects.nonNull(model)) {
                model.setHasRead(true);
                models.add(model);
            }
        });

        if (CollUtil.isNotEmpty(models)) {
            baseMapper.updateBatch(models);
        }
    }
}
