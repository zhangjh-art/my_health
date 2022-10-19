package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SchoolTeacherDTO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SchoolTeacherConvert;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherRespVO;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.mapper.SchoolTeacherMapper;
import com.cnasoft.health.userservice.mapper.UserDynamicMapper;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.model.SchoolTeacher;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISchoolTeacherService;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.service.ITemplateFileService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 校心理老师业务相关
 *
 * @author Shadow
 * @date 2022/4/14 10:00
 */
@Service
public class SchoolTeacherServiceImpl extends SuperServiceImpl<SchoolTeacherMapper, SchoolTeacher> implements ISchoolTeacherService {
    @Value("${user.password.key}")
    private String key;

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
    RocketMQTemplate rocketMQTemplate;

    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Resource
    private UserDynamicMapper userDynamicMapper;

    /**
     * param：①id/手机号/账号状态（用户表） ③工号（老师表）
     *
     * @param params 查询条件
     * @return 分页数据
     */
    @Override
    public PageResult<SchoolTeacherRespVO> findList(Map<String, Object> params) {
        Page<SchoolTeacher> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        params.put("schoolId", UserUtil.getSchoolId());

        List<SchoolTeacher> teachers = baseMapper.findList(page, params, key);
        List<SchoolTeacherRespVO> teacherList = SchoolTeacherConvert.INSTANCE.convert2List(teachers);
        // 账号名(手机号)脱敏
        teacherList.forEach(UserUtil::desensitizedMobile);

        return PageResult.<SchoolTeacherRespVO>builder().data(teacherList).count(page.getTotal()).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SchoolTeacher add(SchoolTeacherReqVO teacher) throws Exception {
        Long schoolId = UserUtil.getSchoolId();
        SchoolTeacher schoolTeacher = SchoolTeacherConvert.INSTANCE.convert(teacher);
        schoolTeacher.setSchoolId(schoolId);

        SysUser user = SysUserConvert.INSTANCE.convertWithSchoolTeacher(teacher);
        user.setSchoolId(schoolId);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());
        user.setIsDeleted(true);

        boolean result = userService.saveUserPublic(user, RoleEnum.school_psycho_teacher);
        if (result) {
            Long userId = user.getId();
            //保存校心理老师
            schoolTeacher.setUserId(userId);
            if (ObjectUtils.isEmpty(schoolTeacher.getIsAcceptTask())) {
                schoolTeacher.setIsAcceptTask(false);
            }
            schoolTeacher.setIsDeleted(true);
            baseMapper.insert(schoolTeacher);

            if (ObjectUtils.isNotEmpty(schoolTeacher.getIsAcceptTask()) && Boolean.TRUE.equals(schoolTeacher.getIsAcceptTask())) {
                //将其他用户的是否承接任务属性设置为false
                baseMapper.setAcceptTaskForOtherTeacher(schoolId, schoolTeacher.getId());
            }

            // 对心理教研员的资质认证文件进行持久化和更新操作
            List<String> fileTempUrls = teacher.getCertificationFile();
            if (CollUtil.isNotEmpty(fileTempUrls)) {
                schoolTeacher.setCertificationFile(fileService.persistedFileUrls(fileTempUrls));
                baseMapper.updateById(schoolTeacher);
            }

            if (ObjectUtils.isNotEmpty(schoolTeacher.getIsAcceptTask()) && Boolean.TRUE.equals(schoolTeacher.getIsAcceptTask())) {
                // 重新指派任务
                String transactionId = UUID.randomUUID().toString();
                TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
                msgDefinationDTO.setDestClass(this.getClass());
                msgDefinationDTO.setMethod("doAdd");
                msgDefinationDTO.setArg(schoolTeacher);

                Map<String, Object> params = new HashMap<>();
                params.put("schoolId", schoolTeacher.getSchoolId());
                params.put("userId", schoolTeacher.getUserId());
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
                doAdd(schoolTeacher, null);
            }
        }
        return schoolTeacher;
    }

    @Transactional(rollbackFor = Exception.class)
    public void doAdd(SchoolTeacher teacher, String transactionId) throws Exception {
        userDynamicMapper.fullData(teacher.getUserId());
        baseMapper.fullData(teacher.getId());
        if (transactionId != null) {
            //记录mq事务日志
            RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
            //这个log字段自定义消息，系统不会使用
            mqLog.setLog("SchoolTeacher json ： " + JsonUtils.writeValueAsString(teacher));
            mqLog.setTransactionId(transactionId);
            rocketmqTransactionLogMapper.insert(mqLog);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SchoolTeacher update(SchoolTeacherReqVO updateReqVO) throws Exception {
        Long schoolId = UserUtil.getSchoolId();
        SchoolTeacher oldTeacher = teacherExists(updateReqVO.getId(), schoolId);
        Assert.notNull(oldTeacher, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        if (ObjectUtils.isNotEmpty(updateReqVO.getIsAcceptTask()) && Boolean.TRUE.equals(updateReqVO.getIsAcceptTask())) {
            // 重新指派任务
            //            reconfirmTaskHandler(schoolId, oldTeacher.getUserId());

            String transactionId = UUID.randomUUID().toString();
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("doUpdate");
            msgDefinationDTO.setArg(updateReqVO);

            Map<String, Object> params = new HashMap<>();
            params.put("schoolId", schoolId);
            params.put("userId", oldTeacher.getUserId());
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
            doUpdate(updateReqVO, null);
        }

        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void doUpdate(SchoolTeacherReqVO updateReqVO, String transactionId) throws Exception {
        Long schoolId = UserUtil.getSchoolId();
        SchoolTeacher oldTeacher = teacherExists(updateReqVO.getId(), schoolId);
        Assert.notNull(oldTeacher, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        // 更新用户信息
        SysUser user = SysUserConvert.INSTANCE.convertWithSchoolTeacher(updateReqVO);
        user.setId(oldTeacher.getUserId());
        userService.updateUserPublic(user);

        SchoolTeacher schoolTeacher = SchoolTeacherConvert.INSTANCE.convert(updateReqVO);
        if (ObjectUtils.isNotEmpty(schoolTeacher.getIsAcceptTask()) && Boolean.TRUE.equals(schoolTeacher.getIsAcceptTask())) {
            //将其他用户的是否承接任务属性设置为false
            baseMapper.setAcceptTaskForOtherTeacher(schoolId, schoolTeacher.getId());
        }
        schoolTeacher.setCertificationFile(fileService.persistedFileUrls(updateReqVO.getCertificationFile()));
        baseMapper.updateById(schoolTeacher);

        // 缓存用户数据
        userService.cacheUser(user.getId());

        schoolTeacher.setSchoolId(schoolId);
        schoolTeacher.setUserId(oldTeacher.getUserId());

        if (transactionId != null) {
            //记录mq事务日志
            RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
            //这个log字段自定义消息，系统不会使用
            mqLog.setLog("SchoolTeacher json ： " + JsonUtils.writeValueAsString(updateReqVO));
            mqLog.setTransactionId(transactionId);
            rocketmqTransactionLogMapper.insert(mqLog);
        }
    }

    /**
     * 根据id删除老师、用户、权限
     *
     * @param ids 老师ID
     * @return 受影响的行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchOperationTipDTO> delete(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Long schoolId = UserUtil.getSchoolId();

            for (Long id : ids) {
                SchoolTeacher schoolTeacher = teacherExists(id, schoolId);
                if (ObjectUtils.isEmpty(schoolTeacher)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                LambdaQueryWrapper<SchoolTeacher> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(SchoolTeacher::getSchoolId, schoolId);
                deleteWrapper.eq(SchoolTeacher::getId, id);
                baseMapper.delete(deleteWrapper);

                userService.delUser(Collections.singleton(schoolTeacher.getUserId()));
            }
        }
        return resultMap;
    }

    @Override
    public SchoolTeacherRespVO findByUserId(Long userId) {
        SchoolTeacher schoolTeacher = baseMapper.findByUserId(userId, key);
        SchoolTeacherRespVO teacherResp = SchoolTeacherConvert.INSTANCE.convert(schoolTeacher);
        // 账号名(手机号)脱敏
        UserUtil.desensitizedMobile(teacherResp);
        return teacherResp;
    }

    @Override
    public void updateCurrentSchoolTeacher(SchoolTeacherReqVO teacherReqVO) throws Exception {
        if (StringUtils.isEmpty(teacherReqVO.getVerifyCode())) {
            //未修改手机号
            teacherReqVO.setMobile(null);
        } else {
            //校验手机号
            smsRecordService.checkCaptcha(teacherReqVO.getMobile(), teacherReqVO.getVerifyCode());
        }
        Long userId = UserUtil.getUserId();
        SchoolTeacher schoolTeacherOld = baseMapper.findByUserId(userId, key);
        Assert.notNull(schoolTeacherOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());
        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(schoolTeacherOld.getUserId());
        user.setName(teacherReqVO.getName());
        user.setSex(teacherReqVO.getSex());
        user.setEmail(teacherReqVO.getEmail());
        user.setMobile(teacherReqVO.getMobile());
        user.setHeadImgUrl(teacherReqVO.getHeadImgUrl());
        user.setNickname(teacherReqVO.getNickname());
        userService.updateUserPublic(user);
        SchoolTeacher schoolTeacher = new SchoolTeacher();
        schoolTeacher.setId(schoolTeacherOld.getId());
        schoolTeacher.setTitle(teacherReqVO.getTitle());
        schoolTeacher.setMajor(teacherReqVO.getMajor());
        schoolTeacher.setSpecialty(teacherReqVO.getSpecialty());
        schoolTeacher.setCertificationFile(fileService.persistedFileUrls(teacherReqVO.getCertificationFile()));
        baseMapper.updateById(schoolTeacher);
        // 缓存用户数据
        userService.cacheUser(user.getId());
    }

    @Override
    public void deleteBySchoolId(Long schoolId) {
        LambdaQueryWrapper<SchoolTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolTeacher::getSchoolId, schoolId);
        baseMapper.delete(wrapper);

    }

    /**
     * 根据老师id查看数据是否存在
     *
     * @param id 学校ID
     * @return 校心理老师对象
     */
    public SchoolTeacher teacherExists(Long id, Long schoolId) {
        return baseMapper.findByIdAndSchoolId(id, schoolId, key);
    }

    @Override
    public void reconfirmTaskHandler(Long schoolId, Long userId) {
        taskExecutor.execute(() -> taskFeign.reconfirmTaskHandler(schoolId, null, userId));
    }

    @Override
    public List<Map<String, Object>> getSelectListByReservationConfig(Long schoolId, Integer weekDay, String startTime, String endTime, Date date) {
        return baseMapper.selectDropDownList(schoolId, weekDay, startTime, endTime, date, key);
    }

    @Override
    public Long findTaskHandlerIdBySchoolId(Long schoolId) {
        return baseMapper.findTaskHandlerIdBySchoolId(schoolId);
    }

    @Override
    public Long getSchoolPsychoTeacherSchoolId(Long userId) {
        return baseMapper.getSchoolPsychoTeacherSchoolId(userId);
    }

    @Override
    public SchoolTeacherDTO findSchoolTeacherInfo(Long userId) {
        SchoolTeacherDTO schoolTeacher = baseMapper.findSchoolTeacherInfo(userId);
        if (Objects.nonNull(schoolTeacher) && schoolTeacher.isSchool()) {
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
            Long schoolId = schoolTeacher.getSchoolId();
            if (schoolMap.containsKey(schoolId)) {
                schoolTeacher.setSchoolName(schoolMap.get(schoolId).getName());
            }
        }
        return schoolTeacher;
    }

    @Override
    public List<CommonDTO> getSchoolPsychoTeacher(Long schoolId) {
        return baseMapper.getSchoolPsychoTeacher(schoolId, key);
    }
}
