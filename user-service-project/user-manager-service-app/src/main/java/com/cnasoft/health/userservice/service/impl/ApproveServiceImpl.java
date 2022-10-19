package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.ApproveDTO;
import com.cnasoft.health.common.dto.ApproveSimpleDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.common.util.time.DateUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.convert.ApproveConvert;
import com.cnasoft.health.userservice.feign.dto.ApplicantUserInfoVO;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.mapper.ApproveMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.impl.approve.handler.ApproveHandleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 * @description 针对表【approve】的数据库操作Service实现
 * @createDate 2022-04-13 11:04:57
 */
@Service
public class ApproveServiceImpl extends SuperServiceImpl<ApproveMapper, Approve> implements IApproveService {

    @Value("${user.password.key}")
    private String key;
    @Resource
    private IApproveService approveService;
    @Resource
    private ApproveConvert convert;
    @Resource
    ApplicationContext applicationContext;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean insertBatch(List<ApproveDTO> approves) {
        Assert.isTrue(CollUtil.isNotEmpty(approves), "缺失数据");

        List<Approve> models = new ArrayList<>(approves.size());
        Integer approveType = approves.get(0).getApproveType();
        List<Long> businessIds = new ArrayList<>(approves.stream().map(ApproveDTO::getBusinessId).collect(Collectors.toSet()));
        Assert.isTrue(CollUtil.isNotEmpty(businessIds), "缺失业务id");

        Map<Long, ApproveSimpleDTO> approveMap = new HashMap<>(16);
        List<ApproveSimpleDTO> approveList = getApproveList(approveType, businessIds);
        if (CollUtil.isNotEmpty(approveList)) {
            approveMap = approveList.stream().collect(Collectors.toMap(ApproveSimpleDTO::getBusinessId, e -> e, (key1, key2) -> key2));
        }

        for (ApproveDTO approve : approves) {
            Approve model = new Approve();
            Long businessId = model.getBusinessId();

            model.setCreateBy(approve.getCreateBy());
            model.setApproveType(approve.getApproveType());
            model.setOperation(approve.getOperation());
            model.setApproveStatus(approve.getApproveStatus());
            model.setBusinessId(approve.getBusinessId());
            model.setBeforeString(approve.getBeforeString());
            model.setAfterString(approve.getAfterString());

            if (approveMap.containsKey(businessId)) {
                ApproveSimpleDTO approveSimple = approveMap.get(businessId);
                if (ApproveStatus.TO_BE_APPROVED.equals(approveSimple.getApproveStatus())) {
                    throw exception("该数据正在审核中，不可进行操作！");
                }
            }
            models.add(model);
        }

        return approveService.saveBatch(models);
    }

    @Override
    public PageResult<ApproveVO> listByParam(Map<String, Object> params) {
        Integer pageNum = MapUtil.getInt(params, "pageNum");
        Integer pageSize = MapUtil.getInt(params, "pageSize");
        Assert.isTrue((pageNum != null && pageSize != null), "分页参数不正确");
        Page<Approve> page = new Page<>(pageNum, pageSize);

        Integer type = MapUtil.getInt(params, "type");
        ApproveType approveType = ApproveType.getType(type);
        Assert.notNull(approveType, "type参数不正确");
        String handleBeanName = approveType.getHandleBeanName();
        if (StringUtils.isEmpty(handleBeanName)) {
            return null;
        }

        // 日期查询参数重写
        Long startTime = MapUtil.getLong(params, "startTime");
        if (startTime != null && startTime > -1) {
            String startTimeStr = DateUtil.secondToLocalDateTime(startTime).format(FORMAT);
            params.put("startTime", startTimeStr);
        }
        Long endTime = MapUtil.getLong(params, "endTime");
        if (endTime != null && endTime > -1) {
            String endTimeStr = DateUtil.secondToLocalDateTime(endTime).format(FORMAT);
            params.put("endTime", endTimeStr);
        }

        ApproveHandleService service = (ApproveHandleService) applicationContext.getBean(approveType.getHandleBeanName());
        List<Long> businessId = service.queryApproveBusinessId(params);

        params.put("businessId", businessId);
        String query = MapUtil.getStr(params, "query");
        if (org.apache.commons.lang3.StringUtils.isNumeric(query)) {
            params.put("approveId", query);
            params.put("mobile", query);
        } else {
            params.put("name", query);
        }
        if (!StringUtils.isEmpty(query) && businessId != null && businessId.size() == 0) {
            return PageResult.<ApproveVO>builder().data(new ArrayList<>()).count(0L).build();
        }

        List<Approve> models = baseMapper.listByParam(page, params);
        List<ApproveVO> approveVOList = convert.convert(models);
        long total = page.getTotal();
        approveVOList.forEach(e -> {
            e.setBeforeJson(desensitizedJsonMobile(e.getBeforeJson()));
            e.setAfterJson(desensitizedJsonMobile(e.getAfterJson()));
            service.handleQueryResult(e);
        });

        return PageResult.<ApproveVO>builder().data(approveVOList).count(total).build();
    }

    public String desensitizedJsonMobile(String json) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        Map<String, Object> map = JsonUtils.readValue(json, HashMap.class);
        if (ObjectUtils.isEmpty(map)) {
            return json;
        }
        if (!ObjectUtils.isEmpty(map.get("mobile"))) {
            map.put("mobile", DesensitizedUtil.desensitized(map.get("mobile").toString(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
        }
        if (!ObjectUtils.isEmpty(map.get("username"))) {
            String username = map.get("username").toString();
            if (TextValidator.isMobileExact(username)) {
                map.put("username", DesensitizedUtil.desensitized(username, DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
            } else if (TextValidator.isIdCard(username)) {
                map.put("username", DesensitizedUtil.desensitized(username, DesensitizedUtil.DesensitizedType.ID_CARD));
            }
        }
        return JsonUtils.writeValueAsString(map);
    }

    @Override
    public void addApproveRecord(ApproveType type, ApproveOperation operation, ApproveStatus status, Long businessId, Long applicantUserId, String beforeJson, String afterJson) {
        Map<Long, ApproveSimpleDTO> approveMap = new HashMap<>(16);
        List<ApproveSimpleDTO> approveList = getApproveList(type.getCode(), Collections.singletonList(businessId));
        if (CollUtil.isNotEmpty(approveList)) {
            approveMap = approveList.stream().collect(Collectors.toMap(ApproveSimpleDTO::getBusinessId, e -> e, (key1, key2) -> key2));
        }

        if (approveMap.containsKey(businessId)) {
            ApproveSimpleDTO approveSimple = approveMap.get(businessId);
            if (ApproveStatus.TO_BE_APPROVED.equals(approveSimple.getApproveStatus())) {
                throw exception("该数据正在审核中，不可进行操作！");
            }
        }

        Approve approve = new Approve();
        approve.setApproveType(type.getCode());
        approve.setOperation(operation.getCode());
        approve.setApproveStatus(status.getCode());
        approve.setBusinessId(businessId);
        approve.setCreateBy(applicantUserId);
        approve.setBeforeString(beforeJson);
        approve.setAfterString(afterJson);

        approveService.save(approve);
    }

    @Override
    public void addApproveRecord(ApproveType type, ApproveOperation operation, ApproveStatus status, Long businessId, Long applicantUserId) {
        addApproveRecord(type, operation, status, businessId, applicantUserId, null, null);
    }

    @Override
    public List<ApplicantUserInfoVO> getApplicantUserList(String type) {
        return baseMapper.getApplicantUserList(type, key);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchOperationTipDTO handleApprove(Long approveRecordId, boolean allow, String rejectReason) {
        try {
            Approve approve = baseMapper.selectById(approveRecordId);
            Assert.isTrue(approve != null, "未找到待审核记录信息");
            Assert.isTrue(ApproveStatus.TO_BE_APPROVED.getCode().equals(approve.getApproveStatus()), "非法的审核状态");
            approve.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
            approve.setApproveRemark(allow ? null : rejectReason);

            ApproveType approveType = ApproveType.getType(approve.getApproveType());
            assert approveType != null;
            ApproveHandleService service = (ApproveHandleService) applicationContext.getBean(approveType.getHandleBeanName());

            ApproveOperation operation = ApproveOperation.getOperation(approve.getOperation());
            assert operation != null;

            switch (operation) {
                case ADD:
                    service.handleAddApprove(approve, allow);
                    break;
                case DELETE:
                    if (allow) {
                        service.handleDeleteApprove(approve);
                    }
                    break;
                case UPDATE:
                    if (allow) {
                        service.handleUpdateApprove(approve);
                    }
                    break;
                case ENABLE:
                case DISABLE:
                    if (allow) {
                        service.handleEnableApprove(approve);
                    }
                    break;
                default:
                    break;
            }
            // 量表审核在分布式事务中完成
            if (approveType == ApproveType.GAUGE) {
                if (operation == ApproveOperation.ADD) {
                    return null;
                }else if(allow){
                    return null;
                }
            }
            saveApproveInfo(approve, allow, rejectReason);
            return null;
        } catch (Exception e) {
            return new BatchOperationTipDTO(approveRecordId, e.getMessage());
        }
    }

    private void saveApproveInfo(Approve approve, boolean allow, String rejectReason) {
        approve.setApproveTime(new Date());
        approve.setApproveUserId(SysUserUtil.getHeaderUserId());
        approveService.saveOrUpdate(approve);
    }

    @Override
    public CommonResult<Integer> selectApproveCount(Integer approveType, Integer operation, Integer approveStatus, List<Long> resourceIds) {
        QueryWrapper<Approve> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Approve::getApproveType, approveType).eq(Approve::getOperation, operation).eq(Approve::getApproveStatus, approveStatus)
                .in(Approve::getBusinessId, resourceIds);
        return CommonResult.success(baseMapper.selectCount(queryWrapper));
    }

    @Override
    public List<ApproveSimpleDTO> getApproveList(Integer approveTypeCode, List<Long> businessIds) {
        ApproveType type = ApproveType.getType(approveTypeCode);
        if (type == null) {
            return null;
        }

        return baseMapper.getApproveList(approveTypeCode, businessIds);
    }

    @Override
    public Approve getApprove(Integer approveType, Long businessId) {
        ApproveType type = ApproveType.getType(approveType);
        if (type == null) {
            return null;
        }

        LambdaQueryWrapper<Approve> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Approve::getApproveType, approveType);
        queryWrapper.eq(Approve::getBusinessId, businessId);
        queryWrapper.orderByDesc(Approve::getId);
        return baseMapper.selectOne(queryWrapper);
    }
}
