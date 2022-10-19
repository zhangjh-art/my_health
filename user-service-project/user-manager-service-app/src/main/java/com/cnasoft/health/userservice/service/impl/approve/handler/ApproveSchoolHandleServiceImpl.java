package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.convert.SchoolConvert;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.mapper.SchoolMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.service.ISchoolService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
import com.google.common.collect.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * @author Administrator
 */
@Service(ApproveBeanName.APPROVE_SCHOOL_HANDLER)
public class ApproveSchoolHandleServiceImpl implements ApproveHandleService {
    @Resource
    ISchoolService schoolService;
    @Resource
    SchoolMapper schoolMapper;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) {
        School school = schoolMapper.selectById(approve.getBusinessId());
        Assert.notNull(school, BAD_REQUEST.getMessage());
        school.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        schoolMapper.updateById(school);

        if (allow) {
            DataCacheUtil.updateSchoolCache(school.getId(), SchoolConvert.INSTANCE.convert(school));
        }
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        schoolService.deleteSchool(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleUpdateApprove(Approve approve) {
        School school = JsonUtils.readValue(approve.getAfterString(), School.class);
        assert school != null;
        Assert.notNull(school, BAD_REQUEST.getMessage());
        if (!ApproveStatus.APPROVED.getCode().equals(school.getApproveStatus())) {
            school.setApproveStatus(ApproveStatus.APPROVED.getCode());
        }
        schoolMapper.updateById(school);

        DataCacheUtil.updateSchoolCache(school.getId(), SchoolConvert.INSTANCE.convert(school));
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String query = MapUtil.getStr(params, "query");
        if (StringUtils.isEmpty(query)) {
            return null;
        }
        List<Long> result = schoolMapper.getApproveAreaId(query);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public void handleQueryResult(ApproveVO approve) {
        approve.setBeforeJson(handleQueryResult(approve.getBeforeJson()));
        approve.setAfterJson(handleQueryResult(approve.getAfterJson()));
    }

    private String handleQueryResult(String json) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        School school = JsonUtils.readValue(json, School.class);
        if (Objects.isNull(school)) {
            return json;
        }

        SchoolDTO schoolDTO = new SchoolDTO();
        BeanUtils.copyProperties(school, schoolDTO);
        //设置区/县名
        Integer areaCode = schoolDTO.getAreaCode();
        if (Objects.isNull(areaCode) || areaCode.toString().length() != 6) {
            return json;
        }
        schoolDTO.setDistinct(areaCode);
        //设置市名
        schoolDTO.setCity(areaCode / 100 * 100);
        //设置省
        schoolDTO.setProvince(areaCode / 10000 * 10000);

        return JsonUtils.writeValueAsString(schoolDTO);
    }
}
