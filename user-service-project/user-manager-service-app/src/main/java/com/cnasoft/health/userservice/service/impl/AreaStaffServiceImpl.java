package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.AreaStaffDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.AreaStaffType;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.AreaStaffConvert;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.feign.dto.AreaStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherRespVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.mapper.AreaStaffMapper;
import com.cnasoft.health.userservice.model.AreaStaff;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IAreaStaffService;
import com.cnasoft.health.userservice.service.IAreaTeacherService;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 区域职员
 *
 * @author ganghe
 */
@Service
public class AreaStaffServiceImpl extends SuperServiceImpl<AreaStaffMapper, AreaStaff> implements IAreaStaffService {

    @Value("${user.password.key}")
    private String key;

    @Resource
    private ISysUserService userService;

    @Resource
    private ISmsRecordService smsRecordService;

    @Resource
    private IAreaTeacherService areaTeacherService;

    @Override
    public PageResult<AreaStaffRespVO> findList(Map<String, Object> params) {
        Page<AreaStaff> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        params.put("areaCode", UserUtil.getAreaCode());

        List<AreaStaff> areaStaffs = baseMapper.findList(page, params, key);
        List<AreaStaffRespVO> areaStaffList = AreaStaffConvert.INSTANCE.convertList(areaStaffs);
        // 账号名(手机号)脱敏
        areaStaffList.forEach(UserUtil::desensitizedMobile);

        return PageResult.<AreaStaffRespVO>builder().data(areaStaffList).count(page.getTotal()).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AreaStaff saveAreaStaff(AreaStaffReqVO areaStaffReqVO) throws Exception {
        Integer areaCode = UserUtil.getAreaCode();
        AreaStaff areaStaff = AreaStaffConvert.INSTANCE.convert(areaStaffReqVO);
        areaStaff.setAreaCode(areaCode);

        SysUser user = SysUserConvert.INSTANCE.convertWithAreaStaff(areaStaffReqVO);
        user.setAreaCode(areaCode);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());

        RoleEnum roleEnum;
        Integer staffType = areaStaff.getType();
        if (AreaStaffType.LEADER.getCode().equals(staffType)) {
            roleEnum = RoleEnum.region_leader;
        } else {
            roleEnum = RoleEnum.region_staff;
        }

        boolean result = userService.saveUserPublic(user, roleEnum);
        if (result) {
            Long userId = user.getId();
            //保存区域职员
            areaStaff.setUserId(userId);
            baseMapper.insert(areaStaff);
        }
        return areaStaff;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAreaStaff(AreaStaffReqVO staffReqVO) throws Exception {
        AreaStaff oldStaff = existsStaff(staffReqVO.getId(), UserUtil.getAreaCode());
        Assert.notNull(oldStaff, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        // 更新用户信息
        SysUser user = SysUserConvert.INSTANCE.convertWithAreaStaff(staffReqVO);
        user.setId(oldStaff.getUserId());

        Integer staffType = staffReqVO.getType();
        if (Objects.nonNull(staffType) && !oldStaff.getType().equals(staffType)) {
            RoleEnum roleEnum;
            if (AreaStaffType.LEADER.getCode().equals(staffType)) {
                roleEnum = RoleEnum.region_leader;
            } else {
                roleEnum = RoleEnum.region_staff;
            }
            user.setRoleCode(roleEnum.getValue());
        }

        userService.updateUserPublic(user);

        AreaStaff areaStaff = AreaStaffConvert.INSTANCE.convert(staffReqVO);
        baseMapper.updateById(areaStaff);

        // 缓存用户数据
        userService.cacheUser(user.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchOperationTipDTO> deleteAreaStaff(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Integer areaCode = UserUtil.getAreaCode();

            for (Long id : ids) {
                AreaStaff areaStaff = existsStaff(id, areaCode);
                if (ObjectUtils.isEmpty(areaStaff)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                LambdaQueryWrapper<AreaStaff> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(AreaStaff::getAreaCode, areaCode);
                deleteWrapper.eq(AreaStaff::getId, id);
                baseMapper.delete(deleteWrapper);

                userService.delUser(Collections.singleton(areaStaff.getUserId()));
            }
        }

        return resultMap;
    }

    /**
     * 根据id和区域编码查看数据是否存在
     *
     * @param id       区域职员ID
     * @param areaCode 区域编码
     * @return 区域职员对象
     */
    private AreaStaff existsStaff(Long id, Integer areaCode) {
        LambdaQueryWrapper<AreaStaff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaStaff::getId, id);
        queryWrapper.eq(AreaStaff::getAreaCode, areaCode);

        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public AreaStaffRespVO findByUserId(Long userId) {
        AreaStaff areaStaff = baseMapper.findByUserId(userId, key);
        AreaStaffRespVO staffResp = AreaStaffConvert.INSTANCE.convertVO(areaStaff);

        // 账号名(手机号)脱敏
        UserUtil.desensitizedMobile(staffResp);

        return staffResp;
    }

    @Override
    public void updateCurrentAreaStaff(AreaStaffReqVO staffReqVO) throws Exception {
        if (StringUtils.isEmpty(staffReqVO.getVerifyCode())) {
            //未修改手机号
            staffReqVO.setMobile(null);
        } else {
            //校验手机号
            smsRecordService.checkCaptcha(staffReqVO.getMobile(), staffReqVO.getVerifyCode());
        }
        Long userId = UserUtil.getUserId();
        AreaStaff areaStaffOld = baseMapper.findByUserId(userId, key);
        Assert.notNull(areaStaffOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());
        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(areaStaffOld.getUserId());
        user.setName(staffReqVO.getName());
        user.setSex(staffReqVO.getSex());
        user.setEmail(staffReqVO.getEmail());
        user.setMobile(staffReqVO.getMobile());
        user.setHeadImgUrl(staffReqVO.getHeadImgUrl());
        user.setNickname(staffReqVO.getNickname());
        userService.updateUserPublic(user);
        AreaStaff areaStaff = new AreaStaff();
        areaStaff.setId(areaStaffOld.getId());
        areaStaff.setDepartment(staffReqVO.getDepartment());
        areaStaff.setPost(staffReqVO.getPost());
        baseMapper.updateById(areaStaff);
        // 缓存用户数据
        userService.cacheUser(user.getId());
    }

    @Override
    public void deleteByAreaCode(Integer areaCode) {
        LambdaQueryWrapper<AreaStaff> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AreaStaff::getAreaCode, areaCode);
        baseMapper.delete(deleteWrapper);

    }

    @Override
    public List<Map<String, Object>> getSelectList(Integer areaCode, String userName) {
        return baseMapper.getSelectList(areaCode, userName, key);
    }

    @Override
    public PageResult<StaffMentalFileVO> listMentalFile(Map<String, Object> params) {
        Page<StaffMentalFileVO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        params.put("areaCode", UserUtil.getAreaCode());

        AreaTeacherRespVO teacher = areaTeacherService.findByUserId(UserUtil.getUserId());
        if (teacher == null || !teacher.getIsAcceptTask()) {
            throw exception("非任务承接人无法查看心理档案");
        }
        List<StaffMentalFileVO> staffs = baseMapper.listMentalFile(page, params, key);
        // 账号名(手机号)脱敏
        staffs.forEach(e -> {
            if (StringUtils.isNotBlank(e.getMobile())) {
                e.setMobile(DesensitizedUtil.desensitized(e.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
            }
        });
        return PageResult.<StaffMentalFileVO>builder().data(staffs).count(page.getTotal()).build();
    }

    @Override
    public UserRespVO findUnionUser(Long userId) {
        SysUser sysUser = userService.selectSysUserById(userId);
        if (RoleEnum.region_psycho_teacher.getValue().equals(sysUser.getRoleCode())) {
            return areaTeacherService.findByUserId(userId);
        } else {
            return findByUserId(userId);
        }
    }

    @Override
    public AreaStaffDTO findAreaStaffInfo(Long userId) {
        return baseMapper.findAreaStaffInfo(userId);
    }
}
