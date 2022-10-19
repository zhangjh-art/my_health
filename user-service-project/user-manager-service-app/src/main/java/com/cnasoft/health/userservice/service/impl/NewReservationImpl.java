package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.MessageDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.MessageType;
import com.cnasoft.health.common.enums.ReservationStatusEnum;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.evaluation.feign.WarningFeign;
import com.cnasoft.health.evaluation.feign.dto.WarningRecordFeignDTO;
import com.cnasoft.health.evaluation.feign.dto.WarningUserRecordDTO;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.UserConstant;
import com.cnasoft.health.userservice.feign.dto.AreaStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.NewReservationReqVO;
import com.cnasoft.health.userservice.feign.dto.NewReservationRespVO;
import com.cnasoft.health.userservice.feign.dto.ParentRespVO;
import com.cnasoft.health.userservice.feign.dto.ReservationConfigRespVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentRespVO;
import com.cnasoft.health.userservice.feign.dto.SupplementReservationReqVO;
import com.cnasoft.health.userservice.mapper.ConsultationReportMapper;
import com.cnasoft.health.userservice.mapper.NewReservationMapper;
import com.cnasoft.health.userservice.mapper.ReservationConfigMapper;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.model.ConsultationReport;
import com.cnasoft.health.userservice.model.NewReservation;
import com.cnasoft.health.userservice.model.ReservationConfig;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.service.IAreaStaffService;
import com.cnasoft.health.userservice.service.IConsultationReportService;
import com.cnasoft.health.userservice.service.INewReservationService;
import com.cnasoft.health.userservice.service.IParentService;
import com.cnasoft.health.userservice.service.IReservationConfigService;
import com.cnasoft.health.userservice.service.ISchoolStaffService;
import com.cnasoft.health.userservice.service.IStudentBaseInfoService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.RedisUtils;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author: zjh
 * @created: 2022/7/20
 */
@Service
public class NewReservationImpl extends SuperServiceImpl<NewReservationMapper, NewReservation> implements INewReservationService {

    @Value("${user.password.key}")
    private String key;

    private static final String DICT_STUDY_CONSULT_QUESTION = "StudyConsultQuestion";
    private static final String DICT_INTERPERSONAL_CONSULT_QUESTION = "InterpersonalConsultQuestion";
    private static final String DICT_SELF_CONSULT_QUESTION = "SelfConsultQuestion";
    private static final String DICT_OTHER_CONSULT_QUESTION = "OtherConsultQuestion";

    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMATTER = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private IParentService parentService;

    @Resource
    private IStudentBaseInfoService studentBaseInfoService;

    @Resource
    private IAreaStaffService areaStaffService;

    @Resource
    private ISchoolStaffService schoolStaffService;

    @Resource
    private IReservationConfigService configService;

    @Resource
    private IConsultationReportService reportService;

    @Resource
    private ConsultationReportMapper consultationReportMapper;

    @Resource
    private ReservationConfigMapper configMapper;

    @Resource
    private INewReservationService newReservationService;

    @Resource
    private IDistLock lock;

    @Resource
    WarningFeign warningFeign;

    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Override
    public Long createLocked(NewReservationReqVO vo, Integer isSubstituted) throws Exception {
        checkConsultTypes(vo);
        Object locker = null;
        try {
            //加锁
            locker = lock.tryLock(UserConstant.LOCK_KEY_PSYCHIATRIST + vo.getPsychiatristId());
            return newReservationService.create(vo,isSubstituted);
        } finally {
            lock.unlock(locker);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(NewReservationReqVO vo, Integer isSubstituted) throws Exception {
        NewReservation newReservation = new NewReservation();
        newReservation.setIsSubstituted(isSubstituted);
        Long userId = checkUserId(vo, newReservation);
        checkExist(vo, userId);
        BeanUtil.copyProperties(vo, newReservation);
        if (vo.getUserRoleType() == null) {
            newReservation.setUserRoleType(getUserRoleType(UserUtil.getCurrentUser()));
        }
        // 设置默认状态
        ReservationConfigRespVO config = configService.getByUserId(vo.getPsychiatristId());
        int defaultStatus = config.getAutoConfirm() == 1 ? 1 : 0;
        int status = newReservation.getIsSubstituted() == 1 ? 1 : defaultStatus;
        newReservation.setStatus(status);
        newReservation.setUserId(userId);
        baseMapper.insert(newReservation);

        if (ReservationStatusEnum.CONFIRM.getCode().equals(status)) { // 待确认预约需要发送站内信
            sendMessage(new ArrayList<Long>() {{
                            add(vo.getPsychiatristId());
                        }}, String.format("您有一个待确认的预约: 预约人:%s\n预约时间:%s",
                sysUserService.findByUserId(newReservation.getUserId(), false).getName(),
                DATE_TIME_FORMATTER.get().format(newReservation.getDate()) + " " + handleTime(newReservation.getStartTime()) + "~" + handleTime(newReservation.getEndTime())),
                MessageType.CONFIRM_RESERVATION.getCode(), String.valueOf(newReservation.getId()), newReservation.getUserId());
        }
        if (ReservationStatusEnum.CONFIRMED.getCode().equals(status)) { // 代预约已确认需要发送站内信
            sendMessage(new ArrayList<Long>() {{
                            add(vo.getUserId());
                        }}, String.format("您有一个已确认的预约: 接受时间:%s", DATE_TIME_FORMATTER.get().format(new Date())),
                MessageType.CONFIRMED_RESERVATION.getCode(), String.valueOf(newReservation.getPsychiatristId()),
                newReservation.getPsychiatristId());
        }
        return newReservation.getId();
    }


    private String handleTime(String time) {
        return new StringBuilder(time).insert(2, ":").toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void supplementCreate(SupplementReservationReqVO vo) throws Exception {
        NewReservationReqVO reservationReqVO = new NewReservationReqVO();
        BeanUtil.copyProperties(vo, reservationReqVO);

        checkConsultTypes(reservationReqVO);

        NewReservation newReservation = new NewReservation();
        BeanUtil.copyProperties(reservationReqVO, newReservation);
        newReservation.setIsSubstituted(1);
        newReservation.setStatus(ReservationStatusEnum.DOWN.getCode());
        Long userId = checkUserId(reservationReqVO, newReservation);
        newReservation.setUserId(userId);
        if (reservationReqVO.getUserRoleType() == null) {
            newReservation.setUserRoleType(getUserRoleType(UserUtil.getCurrentUser()));
        }
        if (vo.getIsEarlyWarning() != null && vo.getIsEarlyWarning() == 1) {
            //预警涉及分布式事务，先标记数据已删除，待事务log入库时，修改为业务可用状态
            newReservation.setIsDeleted(true);
        }
        baseMapper.insert(newReservation);
        Long reservationId = newReservation.getId();

        //        ConsultationReportReqVO reportReqVO = new ConsultationReportReqVO();
        ConsultationReport report = new ConsultationReport();
        BeanUtil.copyProperties(vo, report);
        report.setReservationId(reservationId);
        report.setStartTime(vo.getConsultationStartTime());
        report.setEndTime(vo.getConsultationEndTime());
        //这里不用通用接口，就不涉及并发竞争，不需要加锁
        //reportService.create(reportReqVO);
        if (report.getIsEarlyWarning() != null && report.getIsEarlyWarning() == 1) {
            Assert.notNull(vo.getWarningLevel(), "关注等级不能为空");
            Assert.notNull(vo.getConsultResult(), "干预结果不能为空");

            report.setIsDeleted(true);
            reportService.save(report);

            //不再通过feign入库，通过mq消息通知，在另一个服务消费消息
            WarningRecordFeignDTO warningDTO = reportService.addWarningRecord(newReservation, report);
            String transactionId = UUID.randomUUID().toString();
            //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("fullReservationSupplement");
            Map<String, String> params = new HashMap<>();
            params.put("newReservationId", String.valueOf(newReservation.getId()));
            params.put("consultationReportId", String.valueOf(report.getId()));
            msgDefinationDTO.setArg(params);
            TransactionSendResult sendResult = rocketMQTemplate
                .sendMessageInTransaction(Constant.ADD_WARNING_GROUP, Constant.ADD_WARNING_TOPIC,
                    MessageBuilder.withPayload(warningDTO).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                //mq 异常
                throw exception("系统繁忙,请稍后重试");
            }
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                //数据库异常
                throw exception("系统繁忙,请稍后重试");
            }
        } else {
            reportService.save(report);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fullReservationSupplement(Map<String, String> params, String transactionId) {
        //这里才把业务数据入库,或把数据修改为正常状态，并记录rocketmq事务日志用于回查
        String newReservationId = params.get("newReservationId");
        String consultationReportId = params.get("consultationReportId");
        baseMapper.fullData(newReservationId);
        consultationReportMapper.fullData(consultationReportId);
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        mqLog.setLog("newReservation id ： " + newReservationId + ";consultationReport id :" + consultationReportId);
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    @Override
    public void updateLocked(NewReservationReqVO vo) throws Exception {
        Object locker = null;
        try {
            NewReservation newReservation = baseMapper.selectById(vo.getId());
            Assert.notNull(newReservation, "未查询到相应预约数据");
            locker = lock.tryLock(UserConstant.LOCK_KEY_PSYCHIATRIST + newReservation.getPsychiatristId());
            newReservationService.update(vo);
        } finally {
            lock.unlock(locker);
        }
    }

    @Override
    public void update(NewReservationReqVO vo) throws Exception {
        NewReservation newReservation = baseMapper.selectById(vo.getId());
        Assert.notNull(newReservation, "未查询到相应预约数据");
        checkConsultTypes(vo);
        BeanUtil.copyProperties(vo, newReservation, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        baseMapper.updateById(newReservation);
    }

    @Override
    public NewReservationRespVO get(Long id) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");
        boolean hideName =
            !user.getPresetRoleCode().equals(RoleEnum.region_psycho_teacher.getValue()) && !user.getPresetRoleCode().equals(RoleEnum.school_psycho_teacher.getValue());

        NewReservation newReservation = baseMapper.selectById(id);
        NewReservationRespVO respVO = new NewReservationRespVO();

        BeanUtil.copyProperties(newReservation, respVO);

        fillAdditionFields(respVO, newReservation, hideName);
        return respVO;
    }

    @Override
    public List<Map<String, Object>> getReservationUserList(Map<String, Object> params) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        Assert.notNull(params.get("userRoleType"), "用户类型不能为空");
        String userName = params.get("name") == null ? "" : params.get("name").toString();
        Long schoolId = user.getSchoolId();
        Integer areaCode = user.getAreaCode();
        Long clazzId = params.get("clazzId") == null ? null : Long.parseLong(params.get("clazzId").toString());
        String idCard = params.get("identityCardNumber") == null ? null : params.get("identityCardNumber").toString();
        String studentNumber = params.get("studentNumber") == null ? null : params.get("studentNumber").toString();
        String department = params.get("department") == null ? null : params.get("department").toString();
        int userRoleType = Integer.parseInt(params.get("userRoleType").toString());

        List<Map<String, Object>> selectList;
        switch (userRoleType) {
            case 0:
                selectList = studentBaseInfoService.getSelectList(schoolId, userName, clazzId, idCard, studentNumber);
                break;
            case 1:
                selectList = parentService.getSelectList(schoolId, userName);
                break;
            case 2:
                selectList = schoolStaffService.getSelectList(schoolId, userName, department);
                break;
            default:
                selectList = areaStaffService.getSelectList(areaCode, userName);
        }

        return selectList;
    }

    @Override
    public PageResult<NewReservationRespVO> list(Map<String, Object> params, boolean listByTeacher) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        Page<NewReservation> page = new Page<>(MapUtil.getInt(params, "pageNum", 1), MapUtil.getInt(params, "pageSize", 10));
        if (listByTeacher) {
            params.put("psychiatristId", user.getId());
        } else {
            params.put("userId", user.getId());
        }
        if (params.get("date") != null) {
            Date date = new Date();
            date.setTime(Long.parseLong(params.get("date").toString()) * 1000);
            params.put("date", date);
        }

        List<NewReservation> models = baseMapper.list(page, params, key);
        long total = page.getTotal();
        List<NewReservationRespVO> result = convertModelToVO(models, false);

        return PageResult.<NewReservationRespVO>builder().count(total).data(result).build();
    }

    @Override
    public Map<String, List<Map<String, Object>>> getStatisticalData(Integer year, Integer month) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        year = year == null ? cal.get(Calendar.YEAR) : year;
        month = month == null ? cal.get(Calendar.MONTH) + 1 : month + 1;
        List<Map<String, Object>> queryResult = baseMapper.getStatisticalData(year, month, user.getId());

        for (Map<String, Object> map : queryResult) {
            resultMap.computeIfAbsent(map.get("day").toString(), k -> new ArrayList<>());
            List<Map<String, Object>> dayList = resultMap.get(map.get("day").toString());
            Map<String, Object> oneDayMap = new HashMap<>();
            oneDayMap.put("status", map.get("stat").toString());
            oneDayMap.put("num", map.get("num").toString());
            dayList.add(oneDayMap);
        }

        resultMap.forEach((key, value) -> {
            int total = 0;
            for (Map<String, Object> map : value) {
                total += Integer.parseInt(map.get("num").toString());
            }
            Map<String, Object> totalMap = new HashMap<>();
            totalMap.put("total", total + "");
            value.add(totalMap);
        });
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String remark, String cancelOtherReason) throws Exception {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        NewReservation newReservation = baseMapper.selectById(id);
        Assert.notNull(newReservation, "未查询到相应预约数据");
        checkStatus(newReservation.getStatus(), status, user.getPresetRoleCode());

        newReservation.setStatus(status);
        if (ReservationStatusEnum.CANCELED.getCode().equals(status) && !user.getPresetRoleCode().equals(RoleEnum.region_psycho_teacher.getValue())
            && !user.getPresetRoleCode().equals(RoleEnum.school_psycho_teacher.getValue())) {
            newReservation.setCanceledByStudent(1);
        }
        if (ReservationStatusEnum.REFUSED.getCode().equals(status) || ReservationStatusEnum.CANCELED.getCode().equals(status)) {
            newReservation.setRemark(remark);
        }
        newReservation.setCancelOtherReason(StringUtils.isBlank(cancelOtherReason) ? "" : cancelOtherReason);
        baseMapper.updateById(newReservation);

        String operation = null;
        Integer operationCode = null;
        if (status.equals(ReservationStatusEnum.CONFIRMED.getCode())) {
            operation = "已接受";
            operationCode = MessageType.CONFIRMED_RESERVATION.getCode();
        } else if (status.equals(ReservationStatusEnum.REFUSED.getCode())) {
            operation = "被拒绝";
            operationCode = MessageType.REFUSED_RESERVATION.getCode();
        } else if (status.equals(ReservationStatusEnum.CANCELED.getCode())) {
            operation = "被取消";
            operationCode = MessageType.CANCELED_RESERVATION.getCode();
        }
        if (operation != null) {
            Long userId = newReservation.getUserId();
            sendMessage(new ArrayList<Long>() {{
                            add(userId);
                        }}, String.format("您有一个" + operation + "的预约: " + operation.substring(1) + "时间:%s",
                DATE_TIME_FORMATTER.get().format(new Date())), operationCode, String.valueOf(newReservation.getPsychiatristId()), newReservation.getPsychiatristId());
        }
    }

    @Override
    public void updateStatusLocked(Long id, Integer status, String remark, String cancelOtherReason) throws Exception {
        Object locker = null;
        try {
            locker = lock.tryLock(UserConstant.LOCK_KEY_RESERVATION + id);
            newReservationService.updateStatus(id, status, remark, cancelOtherReason);
        } finally {
            lock.unlock(locker);
        }
    }

    @Override
    public PageResult<NewReservationRespVO> getToConfirmedTask() {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");
        Assert.isTrue(user.getPresetRoleCode().equals(RoleEnum.school_psycho_teacher.getValue()) || user.getPresetRoleCode().equals(RoleEnum.region_psycho_teacher.getValue()),
            "校验用户身份失败");

        Map<String, Object> params = new HashMap<>();
        Page<NewReservation> page = new Page<>(MapUtil.getInt(params, "pageNum", 1), MapUtil.getInt(params, "pageSize", 10));
        params.put("psychiatristId", user.getId());
        params.put("status", 0);

        List<NewReservation> models = baseMapper.list(page, params, key);
        long total = page.getTotal();
        return PageResult.<NewReservationRespVO>builder().count(total).data(convertModelToVO(models, false)).build();
    }

    @Override
    public PageResult<NewReservationRespVO> getConfirmedTask() {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        Map<String, Object> params = new HashMap<>();
        Page<NewReservation> page = new Page<>(MapUtil.getInt(params, "pageNum", 1), MapUtil.getInt(params, "pageSize", 10));
        params.put("userId", user.getId());
        params.put("status", 1);

        List<NewReservation> models = baseMapper.list(page, params, key);
        long total = page.getTotal();
        return PageResult.<NewReservationRespVO>builder().count(total).data(convertModelToVO(models, false)).build();
    }

    private Long checkUserId(NewReservationReqVO vo, NewReservation newReservation) {
        Long userId = null;
        if (newReservation.getIsSubstituted() != null) {
            if (newReservation.getIsSubstituted() == 1) {
                Assert.notNull(vo.getUserId(), "代预约时预约人id不能为空");
                SysUserDTO user = sysUserService.findByUserId(vo.getUserId(), false);
                Assert.notNull(user, "获取操作人个人信息失败");
                userId = vo.getUserId();
            } else {
                SysUserDTO user = UserUtil.getCurrentUser();
                Assert.notNull(user, "获取操作人个人信息失败");
                userId = user.getId();
            }
        }
        return userId;
    }

    private void checkStatus(Integer status, Integer newStatus, String roleCode) {
        if (newStatus == 1 && status == 4) { // 老师更改为已确认
            throw exception("预约已取消，无法确认。");
        } else if (newStatus == 3 && status == 4) { // 老师更改为已拒绝
            throw exception("预约已取消，无法拒绝。");
        } else if (newStatus == 4 && (status == 1 || status == 2 || status == 3) && !roleCode.equals(RoleEnum.region_psycho_teacher.getValue()) && !roleCode.equals(
            RoleEnum.school_psycho_teacher.getValue())) { // 用户更改为已取消
            throw exception("预约状态已更改，无法取消。");
        }
    }

    private void checkConsultTypes(NewReservationReqVO vo) {
        if (StringUtils.isNotEmpty(vo.getConsultTypes())) {
            List<SysDictDTO> selfQuestionCodes = RedisUtils.getDictData(DICT_SELF_CONSULT_QUESTION);
            List<SysDictDTO> studyQuestionCodes = RedisUtils.getDictData(DICT_STUDY_CONSULT_QUESTION);
            List<SysDictDTO> interpersonalQuestionCodes = RedisUtils.getDictData(DICT_INTERPERSONAL_CONSULT_QUESTION);
            List<SysDictDTO> otherQuestionCodes = RedisUtils.getDictData(DICT_OTHER_CONSULT_QUESTION);
            String message = "数据词典缺少问题配置项";
            Assert.isTrue(CollUtil.isNotEmpty(selfQuestionCodes), message);
            Assert.isTrue(CollUtil.isNotEmpty(studyQuestionCodes), message);
            Assert.isTrue(CollUtil.isNotEmpty(interpersonalQuestionCodes), message);
            Assert.isTrue(CollUtil.isNotEmpty(otherQuestionCodes), message);
            String selfCodesStr = selfQuestionCodes.stream().map(SysDictDTO::getDictValue).collect(Collectors.joining());
            String studyCodesStr = studyQuestionCodes.stream().map(SysDictDTO::getDictValue).collect(Collectors.joining());
            String interpersonalCodesStr = interpersonalQuestionCodes.stream().map(SysDictDTO::getDictValue).collect(Collectors.joining());
            String otherCodesStr = otherQuestionCodes.stream().map(SysDictDTO::getDictValue).collect(Collectors.joining());
            for (String code : vo.getConsultTypes().split(",")) {
                Assert.isTrue(selfCodesStr.contains(code) || studyCodesStr.contains(code) || interpersonalCodesStr.contains(code) || otherCodesStr.contains(code), "问题配置项不正确");
                if (otherCodesStr.contains(code)) {
                    Assert.notNull(vo.getConsultDescription(), "请描述您的咨询问题");
                }
            }
        }
    }

    private List<NewReservationRespVO> convertModelToVO(List<NewReservation> models, boolean hideName) {
        List<NewReservationRespVO> resultList = new ArrayList<>();
        models.forEach(model -> {
            NewReservationRespVO respVO = new NewReservationRespVO();
            BeanUtil.copyProperties(model, respVO);
            fillAdditionFields(respVO, model, hideName);
            resultList.add(respVO);
        });
        return resultList;
    }

    private void fillAdditionFields(NewReservationRespVO respVO, NewReservation newReservation, boolean hideName) {
        SysUserDTO teacher = sysUserService.findByUserId(newReservation.getPsychiatristId(), false);
        if (Objects.isNull(teacher)) {
            return;
        }

        SysUserDTO user = sysUserService.findByUserId(newReservation.getUserId(), false);
        if (Objects.isNull(user)) {
            return;
        }

        respVO.setPsychiatristName(teacher.getName());
        respVO.setUserName(hideName ? DesensitizedUtil.desensitized(user.getName(), DesensitizedUtil.DesensitizedType.CHINESE_NAME) : user.getName());
        respVO.setHeadImgUrl(user.getHeadImgUrl());
        respVO.setSex(user.getSex());
        respVO.setPresetRoleCode(user.getPresetRoleCode());
        if (user.getPresetRoleCode().equals(RoleEnum.region_staff.getValue()) || user.getPresetRoleCode().equals(RoleEnum.region_leader.getValue())) {
            AreaStaffRespVO staffInfo = areaStaffService.findByUserId(user.getId());
            respVO.setPost(staffInfo.getPost());
            respVO.setDepartment(staffInfo.getDepartment());
        }
        if (user.getPresetRoleCode().equals(RoleEnum.school_staff.getValue()) || user.getPresetRoleCode().equals(RoleEnum.school_leader.getValue()) || user.getPresetRoleCode()
            .equals(RoleEnum.school_head_teacher.getValue())) {
            SchoolStaffRespVO staffInfo = schoolStaffService.findByUserId(user.getId());
            respVO.setPost(staffInfo.getPost());
            respVO.setDepartment(staffInfo.getDepartment());
        }
        if (user.getPresetRoleCode().equals(RoleEnum.student.getValue())) {
            StudentInfoRespVO studentInfo = studentBaseInfoService.infoOnlyById(user.getId());
            respVO.setGrade(studentInfo.getGrade());
            respVO.setClazzId(studentInfo.getClazzId());
            respVO.setBirthday(studentInfo.getBirthday());
        }
        if (user.getPresetRoleCode().equals(RoleEnum.parents.getValue())) {
            ParentRespVO parentInfo = parentService.infoOnlyByUserId(user.getId());
            List<StudentRespVO> students = parentInfo.getStudents();
            if (hideName) {
                for (StudentRespVO studentRespVO : students) {
                    studentRespVO.setName(DesensitizedUtil.desensitized(studentRespVO.getName(), DesensitizedUtil.DesensitizedType.CHINESE_NAME));
                }
            }
            respVO.setStudentInfo(students);
        }

        CommonResult<WarningUserRecordDTO> userWarningRecord = warningFeign.getUserWarningRecord(respVO.getUserId());
        if (userWarningRecord != null && userWarningRecord.getData() != null) {
            Integer warningGrade = userWarningRecord.getData().getWarningGrade();
            respVO.setIsEarlyWarning(warningGrade == null ? 0 : 1);
        }

        // 咨询问题
        List<SysDictDTO> selfQuestions = RedisUtils.getDictData(DICT_SELF_CONSULT_QUESTION);
        List<SysDictDTO> studyQuestions = RedisUtils.getDictData(DICT_STUDY_CONSULT_QUESTION);
        List<SysDictDTO> interpersonalQuestions = RedisUtils.getDictData(DICT_INTERPERSONAL_CONSULT_QUESTION);
        List<SysDictDTO> otherQuestions = RedisUtils.getDictData(DICT_OTHER_CONSULT_QUESTION);
        String[] codeArr = newReservation.getConsultTypes().split(",");
        StringBuilder questionBuilder = new StringBuilder();
        for (String questionCode : codeArr) {
            String codeName = "";
            if (selfQuestions.stream().anyMatch(q -> q.getDictValue().equals(questionCode))) {
                codeName = selfQuestions.stream().filter(q -> q.getDictValue().equals(questionCode)).findFirst().get().getDictName();
            } else if (studyQuestions.stream().anyMatch(q -> q.getDictValue().equals(questionCode))) {
                codeName = studyQuestions.stream().filter(q -> q.getDictValue().equals(questionCode)).findFirst().get().getDictName();
            } else if (interpersonalQuestions.stream().anyMatch(q -> q.getDictValue().equals(questionCode))) {
                codeName = interpersonalQuestions.stream().filter(q -> q.getDictValue().equals(questionCode)).findFirst().get().getDictName();
            } else if (otherQuestions.stream().anyMatch(q -> q.getDictValue().equals(questionCode))) {
                codeName = otherQuestions.stream().filter(q -> q.getDictValue().equals(questionCode)).findFirst().get().getDictName();
            }
            questionBuilder.append(codeName).append(",");
        }
        respVO.setConsultTypeNames(questionBuilder.length() == 0 ? "" : questionBuilder.substring(0, questionBuilder.length() - 1));
    }

    /**
     * 通过mq异步发送站内信
     * 在调用这个方法的地方，启用本地事务，就可以确保前置业务入库成功，消息一定发送成功
     *
     * 消息会在checkLocalTransaction异步执行发送到mq，发送失败会重试
     * 消费端消费可能失败的问题，由消费端处理
     */
    private void sendMessage(Collection<Long> userIds, String messageContent, Integer type, String params, Long createBy) {
        MessageDTO message = new MessageDTO();
        message.setUserIds(userIds);
        message.setType(type);
        message.setContent(messageContent);
        message.setParams(params);
        message.setCreateBy(createBy);
        //这里不入库，mq的checkLocalTransaction会有1分钟延迟，要想不延时，也可以这里也启用线程异步入库，mq入库时确认幂等。
        //如果这里加异步线程保存，业务完成，这里线程异步未完成时，宕机等异常，消息就会丢失。 多一段mq的代码，可以在重启时，检测并重发消息
//        sysUserService.sendMessage(message);

        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        mqLog.setLog("message json ： " + JsonUtils.writeValueAsString(message));
        String transactionId = UUID.randomUUID().toString();
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
        TransactionSendResult sendResult = rocketMQTemplate
            .sendMessageInTransaction("SendMessageGroup", "send-message-topic",
                MessageBuilder.withPayload(message).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(),
                null);
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            //mq 异常
            throw exception("系统繁忙,请稍后重试");
        }
        if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //数据库异常
            throw exception("系统繁忙,请稍后重试");
        }
    }

    private Integer getUserRoleType(SysUserDTO user) {
        int result = 0;
        if (user.getPresetRoleCode().equals(RoleEnum.student.getValue())) {
            result = 0;
        } else if (user.getPresetRoleCode().equals(RoleEnum.parents.getValue())) {
            result = 1;
        } else if (user.getPresetRoleCode().equals(RoleEnum.school_staff.getValue()) || user.getPresetRoleCode().equals(RoleEnum.school_head_teacher.getValue())
            || user.getPresetRoleCode().equals(RoleEnum.school_leader.getValue())) {
            result = 2;
        } else if (user.getPresetRoleCode().equals(RoleEnum.region_staff.getValue()) || user.getPresetRoleCode().equals(RoleEnum.region_leader.getValue())) {
            result = 3;
        }
        return result;
    }

    private void checkExist(NewReservationReqVO vo, Long userId) {
        // 检查是否已经预约过此时段
        LambdaQueryWrapper<NewReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NewReservation::getUserId, userId);
        queryWrapper.eq(NewReservation::getStartTime, vo.getStartTime());
        queryWrapper.eq(NewReservation::getEndTime, vo.getEndTime());
        queryWrapper.eq(NewReservation::getDate, vo.getDate());
        queryWrapper.in(NewReservation::getStatus, 0, 1);
        List<NewReservation> list = baseMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            throw exception("您已预约过此时段，无法再次预约。");
        }
        // 检查老师预约是否已满
        int intervalNum = configMapper.selectOne(new QueryWrapper<ReservationConfig>().eq("user_id", vo.getPsychiatristId())).getIntervalNum();
        LambdaQueryWrapper<NewReservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(NewReservation::getPsychiatristId, vo.getPsychiatristId());
        reservationWrapper.eq(NewReservation::getStartTime, vo.getStartTime());
        reservationWrapper.eq(NewReservation::getEndTime, vo.getEndTime());
        reservationWrapper.eq(NewReservation::getDate, vo.getDate());
        reservationWrapper.in(NewReservation::getStatus, new ArrayList<Integer>() {{
            add(0);
            add(1);
            add(2);
        }});
        if (intervalNum <= baseMapper.selectList(reservationWrapper).size()) {
            throw exception("当前时段咨询师预约已满，请重新预约。");
        }
    }
}
