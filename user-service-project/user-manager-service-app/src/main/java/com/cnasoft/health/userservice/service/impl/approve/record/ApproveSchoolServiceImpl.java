package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveService;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.SchoolReqVO;
import com.cnasoft.health.userservice.mapper.SchoolMapper;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.service.IApproveService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Administrator
 */
@Component(ApproveBeanName.APPROVE_SCHOOL)
public class ApproveSchoolServiceImpl implements ApproveService {

    @Resource
    private IApproveService approveService;
    @Resource
    SchoolMapper schoolMapper;

    @Override
    public void handleAddApproveRecord(Object data) {
        //修改原表审核状态为待审核
        School school = (School) data;
        school.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        schoolMapper.updateById(school);
        school.setApproveStatus(ApproveStatus.APPROVED.getCode());
        //添加审核记录
        approveService.addApproveRecord(ApproveType.SCHOOL, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, school.getId(),
                SysUserUtil.getHeaderUserId(), null, JsonUtils.writeValueAsStringExcludeNull(school));
    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {

        SchoolReqVO reqVO = (SchoolReqVO) args[0];
        School school = schoolMapper.selectById(reqVO.getId());
        Assert.notNull(school, "未查询到学校数据");

        School exist = schoolMapper.selectOne(new QueryWrapper<School>().eq("name", reqVO.getName())
                .eq("area_code", reqVO.getAreaCode()));
        if (Objects.nonNull(exist)) {
            Assert.isTrue(exist.getId().equals(school.getId()), "该区域相同名称学校已存在");
        }

        String beforeJson = JsonUtils.writeValueAsStringExcludeNull(school);
        school.setName(reqVO.getName());
        school.setAreaCode(reqVO.getAreaCode());
        String afterJson = JsonUtils.writeValueAsStringExcludeNull(school);

        //添加审核记录
        approveService.addApproveRecord(ApproveType.SCHOOL, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, reqVO.getId(),
                SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Object> handleDeleteApproveRecord(Object[] args) {
        //添加审核记录
        if (ObjectUtils.isEmpty(args)) {
            return CommonResult.success();
        }
        Set<Long> ids = (Set<Long>) args[0];
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                School school = schoolMapper.selectById(id);
                if (Objects.isNull(school)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                //添加审核记录
                approveService.addApproveRecord(ApproveType.SCHOOL, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id,
                        SysUserUtil.getHeaderUserId(), JsonUtils.writeValueAsStringExcludeNull(school), null);
            }
        }
        return CommonResult.success(resultMap);
    }

}
