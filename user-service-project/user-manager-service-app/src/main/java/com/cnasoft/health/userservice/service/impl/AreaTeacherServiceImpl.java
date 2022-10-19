package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.AreaTeacherDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.AreaTeacherConvert;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherRespVO;
import com.cnasoft.health.userservice.mapper.AreaTeacherMapper;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.mapper.UserDynamicMapper;
import com.cnasoft.health.userservice.model.AreaTeacher;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IAreaTeacherService;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.service.ITemplateFileService;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 区域心理教研员
 *
 * @author ganghe
 */
@Service
public class AreaTeacherServiceImpl extends SuperServiceImpl<AreaTeacherMapper, AreaTeacher> implements IAreaTeacherService {

    @Resource
    private ISysUserService userService;

    @Resource
    private TaskFeign taskFeign;

    @Resource
    private ISmsRecordService smsRecordService;

    @Resource
    private ITemplateFileService fileService;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private UserDynamicMapper userDynamicMapper;

    @Resource
    RocketMQTemplate rocketMQTemplate;

    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Value("${user.password.key}")
    private String key;

    @Override
    public PageResult<AreaTeacherRespVO> findList(Map<String, Object> params) {
        Page<AreaTeacher> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 0), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        params.put("areaCode", UserUtil.getAreaCode());

        List<AreaTeacher> areaTeachers = baseMapper.findList(page, params, key);
        List<AreaTeacherRespVO> areaTeacherList = AreaTeacherConvert.INSTANCE.convertList(areaTeachers);
        // 账号名(手机号)脱敏
        areaTeacherList.forEach(UserUtil::desensitizedMobile);

        return PageResult.<AreaTeacherRespVO>builder().data(areaTeacherList).count(page.getTotal()).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AreaTeacher saveAreaTeacher(AreaTeacherReqVO teacherReqVO) throws Exception {
        Integer areaCode = UserUtil.getAreaCode();
        AreaTeacher areaTeacher = AreaTeacherConvert.INSTANCE.convertDb(teacherReqVO);
        areaTeacher.setAreaCode(areaCode);

        SysUser user = AreaTeacherConvert.INSTANCE.convertSysUser(teacherReqVO);
        user.setAreaCode(areaCode);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());
        user.setIsDeleted(true);

        boolean result = userService.saveUserPublic(user, RoleEnum.region_psycho_teacher);
        if (result) {
            Long userId = user.getId();
            // 保存区域心理教研员
            areaTeacher.setUserId(userId);
            areaTeacher.setIsDeleted(true);
            baseMapper.insert(areaTeacher);

            if (ObjectUtils.isNotEmpty(areaTeacher.getIsAcceptTask()) && Boolean.TRUE.equals(areaTeacher.getIsAcceptTask())) {
                //将其他用户的是否承接任务属性设置为false
                baseMapper.setAcceptTaskForOtherTeacher(areaCode, areaTeacher.getId());
            }

            // 对区域心理教研员的资质认证文件进行持久化和更新操作
            List<String> fileTempUrls = teacherReqVO.getCertificationFile();
            if (CollUtil.isNotEmpty(fileTempUrls)) {
                areaTeacher.setCertificationFile(fileService.persistedFileUrls(fileTempUrls));
                baseMapper.updateById(areaTeacher);
            }

            if (ObjectUtils.isNotEmpty(areaTeacher.getIsAcceptTask()) && Boolean.TRUE.equals(areaTeacher.getIsAcceptTask())) {
                // 重新指派任务
                String transactionId = UUID.randomUUID().toString();
                TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
                msgDefinationDTO.setDestClass(this.getClass());
                msgDefinationDTO.setMethod("saveAreaTeacher");
                msgDefinationDTO.setArg(areaTeacher);

                Map<String, Object> params = new HashMap<>();
                params.put("areaCode", areaTeacher.getAreaCode());
                params.put("userId", areaTeacher.getUserId());
                //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
                TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.RECONFIRM_TASK_TOPIC,
                    MessageBuilder.withPayload(params).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    //mq 异常
                    throw exception("系统繁忙,请稍后重试");
                }
                if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                    //数据库异常
                    throw exception("系统繁忙,请稍后重试");
                }
            } else {
                saveAreaTeacher(areaTeacher, null);
            }
        }
        return areaTeacher;
    }

    @Transactional
    public void saveAreaTeacher(AreaTeacher areaTeacher, String transactionId) {
        userDynamicMapper.fullData(areaTeacher.getUserId());
        baseMapper.fullData(areaTeacher.getId());
        if (transactionId != null) {
            //记录mq事务日志
            RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
            //这个log字段自定义消息，系统不会使用
            mqLog.setLog("areaTeacher json ： " + JsonUtils.writeValueAsString(areaTeacher));
            mqLog.setTransactionId(transactionId);
            rocketmqTransactionLogMapper.insert(mqLog);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AreaTeacher updateAreaTeacher(AreaTeacherReqVO updateReqVO) throws Exception {
        Integer areaCode = UserUtil.getAreaCode();
        AreaTeacher areaTeacherOld = existsTeacher(updateReqVO.getId(), areaCode);
        Assert.notNull(areaTeacherOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        if (ObjectUtils.isNotEmpty(updateReqVO.getIsAcceptTask()) && Boolean.TRUE.equals(updateReqVO.getIsAcceptTask())) {
            // 重新指派任务
            String transactionId = UUID.randomUUID().toString();
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("doUpdateAreaTeacher");
            msgDefinationDTO.setArg(updateReqVO);

            Map<String, Object> params = new HashMap<>();
            params.put("areaCode", areaCode);
            params.put("userId", areaTeacherOld.getUserId());
            //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.RECONFIRM_TASK_TOPIC,
                MessageBuilder.withPayload(params).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                //mq 异常
                throw exception("系统繁忙,请稍后重试");
            }
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                //数据库异常
                throw exception("系统繁忙,请稍后重试");
            }
        } else {
            doUpdateAreaTeacher(updateReqVO, null);
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void doUpdateAreaTeacher(AreaTeacherReqVO updateReqVO, String transactionId) throws Exception {
        Integer areaCode = UserUtil.getAreaCode();
        AreaTeacher areaTeacherOld = existsTeacher(updateReqVO.getId(), areaCode);
        Assert.notNull(areaTeacherOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        // 更新用户信息
        SysUser user = AreaTeacherConvert.INSTANCE.convertSysUser(updateReqVO);
        user.setId(areaTeacherOld.getUserId());
        userService.updateUserPublic(user);

        AreaTeacher areaTeacher = AreaTeacherConvert.INSTANCE.convertDb(updateReqVO);
        if (ObjectUtils.isNotEmpty(areaTeacher.getIsAcceptTask()) && Boolean.TRUE.equals(areaTeacher.getIsAcceptTask())) {
            //将其他用户的是否承接任务属性设置为false
            baseMapper.setAcceptTaskForOtherTeacher(areaCode, areaTeacher.getId());
        }

        // 对区域心理教研员的资质认证文件进行持久化和更新操作
        areaTeacher.setCertificationFile(fileService.persistedFileUrls(updateReqVO.getCertificationFile()));
        baseMapper.updateById(areaTeacher);

        // 缓存用户数据
        userService.cacheUser(user.getId());

        areaTeacher.setAreaCode(areaTeacherOld.getAreaCode());
        areaTeacher.setUserId(areaTeacherOld.getUserId());

        if (transactionId != null) {
            //记录mq事务日志
            RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
            //这个log字段自定义消息，系统不会使用
            mqLog.setLog("areaTeacher json ： " + JsonUtils.writeValueAsString(areaTeacher));
            mqLog.setTransactionId(transactionId);
            rocketmqTransactionLogMapper.insert(mqLog);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchOperationTipDTO> deleteAreaTeacher(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Integer areaCode = UserUtil.getAreaCode();

            for (Long id : ids) {
                AreaTeacher areaTeacher = existsTeacher(id, areaCode);
                if (ObjectUtils.isEmpty(areaTeacher)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                LambdaQueryWrapper<AreaTeacher> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(AreaTeacher::getAreaCode, areaCode);
                deleteWrapper.eq(AreaTeacher::getId, id);
                baseMapper.delete(deleteWrapper);

                userService.delUser(Collections.singleton(areaTeacher.getUserId()));
            }
        }
        return resultMap;
    }

    @Override
    public AreaTeacherRespVO findByUserId(Long userId) {
        AreaTeacher areaTeacher = baseMapper.findByUserId(userId, key);
        AreaTeacherRespVO teacherResp = AreaTeacherConvert.INSTANCE.convertVO(areaTeacher);
        // 账号名(手机号)脱敏
        UserUtil.desensitizedMobile(teacherResp);
        return teacherResp;
    }

    @Override
    public void updateCurrentAreaTeacher(AreaTeacherReqVO teacherReqVO) throws Exception {
        if (StringUtils.isEmpty(teacherReqVO.getVerifyCode())) {
            //未修改手机号
            teacherReqVO.setMobile(null);
        } else {
            //校验手机号
            smsRecordService.checkCaptcha(teacherReqVO.getMobile(), teacherReqVO.getVerifyCode());
        }
        Long userId = UserUtil.getUserId();
        AreaTeacher areaTeacherOld = baseMapper.findByUserId(userId, key);
        Assert.notNull(areaTeacherOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());
        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(areaTeacherOld.getUserId());
        user.setName(teacherReqVO.getName());
        user.setSex(teacherReqVO.getSex());
        user.setEmail(teacherReqVO.getEmail());
        user.setMobile(teacherReqVO.getMobile());
        user.setHeadImgUrl(teacherReqVO.getHeadImgUrl());
        user.setNickname(teacherReqVO.getNickname());
        userService.updateUserPublic(user);
        AreaTeacher areaTeacher = new AreaTeacher();
        areaTeacher.setId(areaTeacherOld.getId());
        areaTeacher.setTitle(teacherReqVO.getTitle());
        areaTeacher.setMajor(teacherReqVO.getMajor());
        areaTeacher.setSpecialty(teacherReqVO.getSpecialty());
        areaTeacher.setCertificationFile(fileService.persistedFileUrls(teacherReqVO.getCertificationFile()));
        baseMapper.updateById(areaTeacher);
        // 缓存用户数据
        userService.cacheUser(user.getId());
    }

    @Override
    public void deleteByAreaCode(Integer areaCode) {
        LambdaQueryWrapper<AreaTeacher> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AreaTeacher::getAreaCode, areaCode);
        baseMapper.delete(deleteWrapper);
    }

    /**
     * 根据id和区域编码查看数据是否存在
     *
     * @param id       区域心理教研员ID
     * @param areaCode 区域编码
     * @return 区域职员对象
     */
    private AreaTeacher existsTeacher(Long id, Integer areaCode) {
        LambdaQueryWrapper<AreaTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaTeacher::getId, id);
        queryWrapper.eq(AreaTeacher::getAreaCode, areaCode);

        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void reconfirmTaskHandler(Integer areaCode, Long userId) {
        taskExecutor.execute(() -> taskFeign.reconfirmTaskHandler(null, areaCode, userId));
    }

    @Override
    public List<Map<String, Object>> getSelectListByReservationConfig(Integer areaCode, Integer weekDay, String startTime, String endTime, Date date) {
        return baseMapper.selectDropDownList(areaCode, weekDay, startTime, endTime, date, key);
    }

    @Override
    public Long findTaskHandlerIdByAreaCode(Integer areaCode) {
        return baseMapper.findTaskHandlerIdByAreaCode(areaCode);
    }

    @Override
    public AreaTeacherDTO findAreaTeacherInfo(Long userId) {
        return baseMapper.findAreaTeacherInfo(userId);
    }

    @Override
    public List<CommonDTO> getAreaPsychoTeacher(Integer areaCode) {
        return baseMapper.getAreaPsychoTeacher(areaCode, key);
    }
}
