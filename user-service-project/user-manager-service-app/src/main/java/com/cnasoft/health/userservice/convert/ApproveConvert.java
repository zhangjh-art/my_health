package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.service.ISysUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Created by lgf on 2022/4/21.
 */
@Component
public class ApproveConvert {

    @Resource
    private ISysUserService userService;

    public ApproveVO convert(Approve model) {
        ApproveVO vo = new ApproveVO();
        // 通用属性赋值
        vo.setId(model.getId().toString());
        vo.setApproveStatus(model.getApproveStatus());
        SysUserDTO applicantUser = userService.findByUserId(model.getCreateBy(), false);
        if (Objects.nonNull(applicantUser)) {
            vo.setApplicantName(applicantUser.getName());
        }
        vo.setApplicantUserId(model.getCreateBy().toString());
        vo.setApplicantDate(model.getCreateTime());
        vo.setRemark(model.getApproveRemark());
        vo.setApproveType(model.getApproveType());
        vo.setBeforeJson(model.getBeforeString());
        vo.setAfterJson(model.getAfterString());
        vo.setApproveOperation(model.getOperation());

        return vo;
    }

    public List<ApproveVO> convert(List<Approve> modelList) {
        List<ApproveVO> result = new ArrayList<>(modelList.size());
        modelList.forEach(model -> result.add(convert(model)));
        return result;
    }
}
