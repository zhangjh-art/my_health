package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.time.DateUtil;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.ConsultationRecordReqVO;
import com.cnasoft.health.userservice.feign.dto.ConsultationRecordRespVO;
import com.cnasoft.health.userservice.mapper.ConsultationRecordMapper;
import com.cnasoft.health.userservice.mapper.SmartConsultQuestionMapper;
import com.cnasoft.health.userservice.model.ConsultationRecord;
import com.cnasoft.health.userservice.model.SmartConsultQuestion;
import com.cnasoft.health.userservice.service.IConsultationRecordService;
import com.cnasoft.health.userservice.util.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ConsultationRecordImpl extends SuperServiceImpl<ConsultationRecordMapper, ConsultationRecord>
    implements IConsultationRecordService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private SmartConsultQuestionMapper smartConsultQuestionMapper;

    @Override
    public void create(ConsultationRecordReqVO vo) {
        Assert.notNull(vo.getSource(), "咨询来源不能为空");
        if (vo.getUserId() == null) {
            SysUserDTO user = UserUtil.getCurrentUser();
            Assert.notNull(user, "获取当前操作人信息失败");
            vo.setUserId(user.getId());
        }
        vo.setDate(new Date());
        QueryWrapper<ConsultationRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", vo.getUserId());
        wrapper.eq("source", vo.getSource());
        wrapper.eq("source_id", vo.getSourceId());
        Calendar cal = Calendar.getInstance();
        cal.setTime(vo.getDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        wrapper.ge("date", cal.getTime());
        ConsultationRecord record = baseMapper.selectOne(wrapper);
        if (record == null) {
            ConsultationRecord newRecord = new ConsultationRecord();
            BeanUtil.copyProperties(vo, newRecord);
            baseMapper.insert(newRecord);
        }
    }

    @Override
    public PageResult<ConsultationRecordRespVO> listByUserId(Map<String, Object> params) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        Page<ConsultationRecord> page =
            new Page<>(MapUtil.getInt(params, "pageNum", 1), MapUtil.getInt(params, "pageSize", 10));
        QueryWrapper<ConsultationRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId());
        wrapper.orderByDesc("date");

        Long startTime = MapUtil.getLong(params, "startTime");
        if (startTime != null && startTime > -1) {
            String startTimeStr = DateUtil.secondToLocalDateTime(startTime).format(FORMAT);
            wrapper.ge("date", startTimeStr);
        }
        Long endTime = MapUtil.getLong(params, "endTime");
        if (startTime != null && startTime > -1) {
            String endTimeStr = DateUtil.secondToLocalDateTime(endTime).format(FORMAT);
            wrapper.le("date", endTimeStr);
        }
        Integer source = MapUtil.getInt(params, "source");
        if(source != null){
            wrapper.eq("source", source);
        }
        Page<ConsultationRecord> queryPage = baseMapper.selectPage(page, wrapper);
        List<ConsultationRecord> models = queryPage.getRecords();

        long total = queryPage.getTotal();
        List<ConsultationRecordRespVO> result = convertModelToVO(models);
        return PageResult.<ConsultationRecordRespVO>builder().count(total).data(result).build();
    }

    private List<ConsultationRecordRespVO> convertModelToVO(List<ConsultationRecord> models) {
        List<ConsultationRecordRespVO> resultList = new ArrayList<>();
        models.forEach(model -> {
            ConsultationRecordRespVO respVO = new ConsultationRecordRespVO();
            BeanUtil.copyProperties(model, respVO);
            if (model.getSource().equals(0)) { // 小精灵 需要返回具体的问题
                SmartConsultQuestion question = smartConsultQuestionMapper.selectById(model.getSourceId());
                respVO.setConsultQuestion(question.getQuestion());
            }
            resultList.add(respVO);
        });
        return resultList;
    }

}
