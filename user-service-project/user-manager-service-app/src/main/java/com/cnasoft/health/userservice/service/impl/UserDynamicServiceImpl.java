package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.DispositionStatus;
import com.cnasoft.health.common.dto.EarlyWarningStatusUpdateReqVO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.TaskTargetRoleEnum;
import com.cnasoft.health.common.enums.WarningLevelEnum;
import com.cnasoft.health.common.enums.WarningSourceEnum;
import com.cnasoft.health.common.enums.WarningStatus;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.CustomConverter;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.evaluation.feign.WarningFeign;
import com.cnasoft.health.evaluation.feign.dto.WarningRecordFeignDTO;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.UserDynamicConvert;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.feign.dto.DynamicWarningRespVO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicDTO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicReqVO;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.mapper.UserDynamicMapper;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.model.UserDynamic;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.service.IUserDynamicService;
import com.cnasoft.health.userservice.util.RedisUtils;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Service
public class UserDynamicServiceImpl extends SuperServiceImpl<UserDynamicMapper, UserDynamic> implements IUserDynamicService {

    @Resource
    UserDynamicMapper userDynamicMapper;
    @Resource
    TaskFeign taskFeign;
    @Resource
    WarningFeign warningFeign;
    @Resource
    ISysUserService sysUserService;
    @Value("${user.password.key}")
    private String key;

    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    private final CustomConverter customConverter = new CustomConverter();

    @Override
    public PageResult<UserDynamicDTO> getUserDynamicPage(Map<String, Object> params) {
        Page<UserDynamic> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1)
                , MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        List<UserDynamicDTO> userDynamicList = userDynamicMapper.selectUserDynamicList(page, params);
        long total = page.getTotal();
        return PageResult.<UserDynamicDTO>builder().data(userDynamicList).count(total).build();
    }

    @Override
    public void saveUserDynamicInfo(UserDynamicReqVO userDynamicVO) {
        UserDynamic userDynamic = UserDynamicConvert.INSTANCE.convertUserDynamicVO(userDynamicVO);
        List<SysDictDTO> dictDTOList = RedisUtils.getDictData(UserUtil.DYNAMIC_WARN);
        Set<String> warnWords = new HashSet<>();
        for (SysDictDTO sysDictDTO : dictDTOList) {
            if (userDynamic.getContent().contains(sysDictDTO.getDictName())) {
                warnWords.add(sysDictDTO.getDictName());
            }
        }
        if (!warnWords.isEmpty()) {
            userDynamic.setIsWarn(1);
            userDynamic.setDealResult(0);
            userDynamic.setWarnWords(JsonUtils.writeValueAsString(warnWords));
        } else {
            userDynamic.setIsWarn(0);
        }
        if (userDynamic.getSleepTime() != null && userDynamic.getGetupTime() != null) {
            long between = DateUtil.between(userDynamic.getGetupTime(), userDynamic.getSleepTime(), DateUnit.MINUTE);
            userDynamic.setSleepMinute((int) between);
        }
        SysUserDTO currentUser = UserUtil.getCurrentUser();
        Assert.notNull(currentUser, "登录异常");
        userDynamic.setUserId(currentUser.getId());
        userDynamic.setUserNickName(currentUser.getNickname());
        if (userDynamic.getIsWarn() > 0) {
            //走事务消时，为了获取业务数据id入库已删除数据数据，发送半消息到mq，等mq修改为正常数据
            userDynamic.setIsDeleted(true);
            userDynamicMapper.insert(userDynamic);
            //有预警，走事务消息，不会入库
            WarningRecordFeignDTO dto = new WarningRecordFeignDTO();
            dto.setUserId(userDynamic.getUserId());
            dto.setUserRoleType(getUserRoleType().getCode());
            dto.setSourceType(WarningSourceEnum.USER_DYNAMIC.getCode());
            //rel_id userDynamic业务保存后才有数据
            dto.setSourceRelId(userDynamic.getId());
            dto.setWarningStatus(WarningStatus.UNHANDLED.getCode());
            dto.setWarningGrade(WarningLevelEnum.HIGH_WARNING.getCode());
            dto.setCreateBy(0L);
            dto.setCreateTime(new Date());
            //不再通过feign入库，通过mq消息通知，在另一个服务消费消息
//            warningFeign.addWarningRecord(dto);
            String transactionId = UUID.randomUUID().toString();
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("saveUserDynamicInfo");
            msgDefinationDTO.setArg(userDynamic);
            //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, Constant.ADD_WARNING_TOPIC,
                    MessageBuilder.withPayload(dto)
                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .build()
                    , msgDefinationDTO
            );
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                //mq 异常
                throw exception("系统繁忙,请稍后重试");
            }
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                //数据库异常
                throw exception("系统繁忙,请稍后重试");
            }
        } else {
            //没有预警，不涉及分布式数据库，不走事务消息
            userDynamicMapper.insert(userDynamic);
        }
    }

    //这里一定要加本地事务，事务确保业务消息和mqlog入库的原子性
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserDynamicInfo(UserDynamic userDynamic, String transactionId) {
        //这里才把业务数据入库,或把数据修改为正常状态，并记录rocketmq事务日志用于回查
        userDynamicMapper.fullData(userDynamic.getId());
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        mqLog.setLog("userDynamic json ： " + JsonUtils.writeValueAsString(userDynamic));
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    private TaskTargetRoleEnum getUserRoleType() {
        SysUserDTO sysUser = UserUtil.getCurrentUser();
        String roleCode = sysUser.getPresetRoleCode();

        if (roleCode.equals(RoleEnum.student.getValue())) {
            return TaskTargetRoleEnum.STUDENT;
        } else if (roleCode.equals(RoleEnum.parents.getValue())) {
            return TaskTargetRoleEnum.PARENTS;
        } else if (roleCode.equals(RoleEnum.school_staff.getValue()) ||
                roleCode.equals(RoleEnum.school_psycho_teacher.getValue()) ||
                roleCode.equals(RoleEnum.school_head_teacher.getValue()) ||
                roleCode.equals(RoleEnum.school_leader.getValue()) ||
                roleCode.equals(RoleEnum.school_admin.getValue())) {
            return TaskTargetRoleEnum.SCHOOL_STAFF;
        } else {
            return TaskTargetRoleEnum.REGION_STAFF;
        }
    }

    @Override
    public void deleteDynamic(Long dynamicId) {
        baseMapper.deleteById(dynamicId);
    }

    @Override
    @Transactional
    public void updateDynamic(Long dynamicId, Integer sort) {
        Long userId = UserUtil.getUserId();
        userDynamicMapper.updateResetSort(userId);
        userDynamicMapper.updateSort(dynamicId, sort);
    }

    @Override
    public PageResult<DynamicWarningRespVO> getSchoolDynamicWarning(Map<String, Object> params) {
        Page<DynamicWarningRespVO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1),
                MapUtil.getInt(params, Constant.PAGE_SIZE, 10));

        //获取校心理老师的学校id
        Long schoolId = UserUtil.getSchoolId();
        params.put("schoolId", schoolId);
        Integer type = (Integer) params.get("type");
        Assert.notNull(type, "查询类型不能为空");

        List<DynamicWarningRespVO> dynamicWarning = new ArrayList<>();
        switch (type) {
            //学生
            case 1:
                dynamicWarning = userDynamicMapper.getStudentDynamicWarning(page, params, key);
                break;
            //家长
            case 2:
                dynamicWarning = userDynamicMapper.getParentDynamicWarning(page, params, key);
                break;
            //老师
            case 3:
                dynamicWarning = userDynamicMapper.getTeacherDynamicWarning(page, params, key);
                break;
            default:
                throw exception("查询类型错误");
        }
        long total = page.getTotal();
        dynamicWarning.forEach(dynamic -> {
            dynamic.setWarnWordList(customConverter.toStrList(dynamic.getWarnWords()));
            SysUserDTO sysUser = sysUserService.findByUserId(dynamic.getUserId(), false);
            dynamic.setPresetRoleCode(sysUser.getPresetRoleCode());
        });
        return PageResult.<DynamicWarningRespVO>builder().data(dynamicWarning).count(total).build();
    }

    @Override
    public void dealSchoolDynamicWarning(EarlyWarningStatusUpdateReqVO vo) {
        //只能处置本校的动态预警
        Long schoolId = UserUtil.getSchoolId();
        UserDynamic model = userDynamicMapper.selectDynamicById(vo.getId(), schoolId, null);
        Assert.notNull(model, "数据不存在");
        if (Objects.equals(model.getIsWarn(), DispositionStatus.UNHANDLED.getCode())) {
            throw exception("无需处置");
        }
        if (model.getDealResult() != null && model.getDealResult() > DispositionStatus.UNHANDLED.getCode()) {
            throw exception("该预警已处置");
        }
        DispositionStatus status = DispositionStatus.getStatus(vo.getStatus());
        model.setDealResult(status.getCode());
        model.setDealDescription(vo.getRemark());

        baseMapper.updateById(model);
    }

    @Override
    public PageResult<DynamicWarningRespVO> getAreaDynamicWarning(Map<String, Object> params) {
        Page<DynamicWarningRespVO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1),
                MapUtil.getInt(params, Constant.PAGE_SIZE, 10));

        //获取区域心理教研员的areaCode
        Integer areaCode = UserUtil.getAreaCode();
        params.put("areaCode", areaCode);

        List<DynamicWarningRespVO> dynamicWarning = userDynamicMapper.getAreaDynamicWarning(page, params, key);

        long total = page.getTotal();
        dynamicWarning.forEach(dynamic -> {
            dynamic.setWarnWordList(customConverter.toStrList(dynamic.getWarnWords()));
            SysUserDTO sysUser = sysUserService.findByUserId(dynamic.getUserId(), false);
            dynamic.setPresetRoleCode(sysUser.getPresetRoleCode());
        });
        return PageResult.<DynamicWarningRespVO>builder().data(dynamicWarning).count(total).build();
    }

    @Override
    public UserDynamicDTO selectUserDynamic(Long id) {
        UserDynamic userDynamic = userDynamicMapper.selectUserDynamic(id);
        UserDynamicDTO userDynamicDTO = new UserDynamicDTO();
        if (userDynamic == null) {
            return null;
        }
        BeanUtils.copyProperties(userDynamic, userDynamicDTO);
        return userDynamicDTO;
    }

    @Override
    public void dealAreaDynamicWarning(EarlyWarningStatusUpdateReqVO vo) {
        //只能处置本区域的动态预警
        Integer areaCode = UserUtil.getAreaCode();
        UserDynamic model = userDynamicMapper.selectDynamicById(vo.getId(), null, areaCode);
        Assert.notNull(model, "数据未找到");
        if (Objects.equals(model.getIsWarn(), DispositionStatus.UNHANDLED.getCode())) {
            throw exception("无需处置");
        }
        if (model.getDealResult() != null && model.getDealResult() > DispositionStatus.UNHANDLED.getCode()) {
            throw exception("该预警已处置");
        }
        DispositionStatus status = DispositionStatus.getStatus(vo.getStatus());
        model.setDealResult(status.getCode());
        model.setDealDescription(vo.getRemark());

        baseMapper.updateById(model);
    }
}
