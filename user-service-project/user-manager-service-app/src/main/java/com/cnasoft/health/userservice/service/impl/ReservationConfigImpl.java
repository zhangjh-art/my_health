package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.userservice.feign.dto.ReservationConfigReqVO;
import com.cnasoft.health.userservice.feign.dto.ReservationConfigRespVO;
import com.cnasoft.health.userservice.mapper.NewReservationMapper;
import com.cnasoft.health.userservice.mapper.ReservationConfigMapper;
import com.cnasoft.health.userservice.mapper.ReservationTimeConfigMapper;
import com.cnasoft.health.userservice.model.NewReservation;
import com.cnasoft.health.userservice.model.ReservationConfig;
import com.cnasoft.health.userservice.model.ReservationTimeConfig;
import com.cnasoft.health.userservice.service.IAreaTeacherService;
import com.cnasoft.health.userservice.service.IReservationConfigService;
import com.cnasoft.health.userservice.service.ISchoolTeacherService;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: zjh
 * @created: 2022/7/19
 */
@Slf4j
@Service
public class ReservationConfigImpl extends SuperServiceImpl<ReservationConfigMapper, ReservationConfig> implements IReservationConfigService {

    @Resource
    private IAreaTeacherService areaTeacherService;

    @Resource
    private ISchoolTeacherService schoolTeacherService;

    @Resource
    private ReservationTimeConfigMapper reservationTimeConfigMapper;

    @Resource
    private NewReservationMapper reservationMapper;

    @Override
    public void create(ReservationConfigReqVO vo) {
        checkExpireDate(vo.getExpireDate());
        ReservationConfig model = new ReservationConfig();
        BeanUtil.copyProperties(vo, model);
        Long userId = SysUserUtil.getHeaderUserId();
        model.setUserId(userId);
        baseMapper.insert(model);

        Map<Integer, List<Map<String, String>>> timeConfigs = vo.getTimeConfig();
        if (timeConfigs != null) {
            addTimeConfig(timeConfigs, model);
        }
    }

    @Override
    public Map<String, List<Map<String, Object>>> update(ReservationConfigReqVO vo) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> reason = new ArrayList<>();
        checkExpireDate(vo.getExpireDate());
        ReservationConfig config = baseMapper.selectById(vo.getId());

        if (!config.getIntervalTime().equals(vo.getIntervalTime())) { // 更改时间间隔校验
            fullTimeConfigCheck(reason, user.getId());
            if (reason.size() > 0) {
                result.put("IntervalChange", reason);
                return result;
            }
        } else { // 更改时间段设置校验
            updateTimeConfigCheck(reason, user.getId(), vo);
            if (reason.size() > 0) {
                result.put("TimeConfigChange", reason);
                return result;
            }
        }

        if (!vo.getIntervalTime().equals(config.getIntervalTime())) {
            reservationTimeConfigMapper.deleteByConfigId(vo.getId());
        }
        BeanUtil.copyProperties(vo, config, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        baseMapper.updateById(config);

        Map<Integer, List<Map<String, String>>> targetTimeConfig = vo.getTimeConfig();
        reservationTimeConfigMapper.deleteByConfigId(vo.getId());
        if (CollUtil.isNotEmpty(targetTimeConfig)) {
            addTimeConfig(targetTimeConfig, config);
        }
        return result;
    }

    @Override
    public ReservationConfigRespVO get() {
        Long userId = SysUserUtil.getHeaderUserId();
        return get(userId);
    }

    @Override
    public ReservationConfigRespVO get(Long userId) {
        ReservationConfigRespVO result = new ReservationConfigRespVO();
        ReservationConfig config = baseMapper.selectOne(new QueryWrapper<ReservationConfig>().eq("user_id", userId));
        if (config == null) {
            return result;
        }

        BeanUtil.copyProperties(config, result);
        // 过期判断(过期不展示时段配置)
        if (new Date().after(config.getExpireDate())) {
            result.setTimeConfig(new HashMap<>());
            return result;
        }

        List<ReservationTimeConfig> timeConfigList = reservationTimeConfigMapper.selectList(new QueryWrapper<ReservationTimeConfig>().eq("config_id", config.getId()));
        Map<Integer, List<ReservationTimeConfig>> timeConfigGroups = timeConfigList.stream().collect(Collectors.groupingBy(ReservationTimeConfig::getWeekDay));
        Map<Integer, List<Map<String, String>>> timeConfigs = new HashMap<>();
        timeConfigGroups.forEach((key, values) -> {
            List<Map<String, String>> dates = new ArrayList<>();
            for (ReservationTimeConfig timeConfig : values) {
                Map<String, String> map = new HashMap();
                map.put("startTime", timeConfig.getStartTime());
                map.put("endTime", timeConfig.getEndTime());
                dates.add(map);
            }
            timeConfigs.put(key, dates);
        });

        result.setTimeConfig(timeConfigs);
        return result;
    }

    @Override
    public ReservationConfigRespVO getByUserId(Long userId) {
        LambdaQueryWrapper<ReservationConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationConfig::getUserId, userId);
        List<ReservationConfig> configModel = baseMapper.selectList(queryWrapper);

        ReservationConfigRespVO result = null;
        if (CollUtil.isNotEmpty(configModel)) {
            result = new ReservationConfigRespVO();
            BeanUtil.copyProperties(configModel.get(0), result);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAvailableTeachers(Map<String, Object> params) {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "获取当前操作人信息失败");

        Integer weekDay = null;
        Date date = null;
        if (params.get("date") != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(Long.parseLong(params.get("date").toString()) * 1000L));
            weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;
            date = getDate(Long.parseLong(params.get("date").toString()));
        }
        String startTime = params.get("start") == null ? "" : params.get("start").toString();
        String endTime = params.get("end") == null ? "" : params.get("end").toString();

        List<Map<String, Object>> teacherList;
        if (!user.isSchool()) {
            teacherList = areaTeacherService.getSelectListByReservationConfig(user.getAreaCode(), weekDay, startTime, endTime, date);
        } else {
            teacherList = schoolTeacherService.getSelectListByReservationConfig(user.getSchoolId(), weekDay, startTime, endTime, date);
        }

        return filterTeacherList(teacherList, date);
    }

    @Override
    public List<Map<String, Object>> getAvailableTimeByTeacherId(Long psychiatristId, Long date) {
        Assert.notNull(psychiatristId, "咨询师id不能为空");
        Assert.notNull(date, "查询时间不能为空");

        QueryWrapper<ReservationConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", psychiatristId);
        wrapper.ge("expire_date", getDate(date));
        ReservationConfig mainConfig;
        List<ReservationConfig> configList = baseMapper.selectList(wrapper);
        if (CollUtil.isNotEmpty(configList)) {
            mainConfig = configList.get(0);
        } else {
            return new ArrayList<>();
        }
        if (mainConfig.getAdvanceTime() != 0) {
            // cal为可预约的最早时间(当前时间+提前时间)
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DAY_OF_MONTH, mainConfig.getAdvanceTime());
            // 如果早于这个时间
            if (date < cal.getTime().getTime() / 1000) {
                return new ArrayList<>();
            }
        }

        LambdaQueryWrapper<ReservationTimeConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationTimeConfig::getUserId, psychiatristId);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date * 1000L));
        int weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;
        queryWrapper.eq(ReservationTimeConfig::getWeekDay, weekDay);
        queryWrapper.eq(ReservationTimeConfig::getConfigId, mainConfig.getId());
        List<ReservationTimeConfig> configs = reservationTimeConfigMapper.selectList(queryWrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        String latestTime = getLatestTime(cal, mainConfig.getIntervalTime());

        for (ReservationTimeConfig config : configs) {
            int reservationNum = getReservationNum(psychiatristId, config.getStartTime(), config.getEndTime(), getDate(date));
            if (reservationNum < mainConfig.getIntervalNum()) {
                // 如果预约今天的时间  那么不能预约已经过去的时间
                if (latestTime == null || latestTime.compareTo(config.getStartTime()) <= 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("startTime", config.getStartTime());
                    map.put("endTime", config.getEndTime());
                    resultList.add(map);
                }
            }
        }
        return resultList.stream().sorted(Comparator.comparing((Map<String, Object> map) -> map.get("startTime").toString())).collect(Collectors.toList());
    }

    @Override
    // 暂时未使用  使用时需再更改
    public List<Map<String, Object>> getAvailableTimeByDate(Long date) {
        SysUserDTO user = UserUtil.getCurrentUser();
        user.setPresetRoleCode(RoleEnum.student.getValue());
        Assert.notNull(user, "获取当前操作人信息失败");
        Assert.notNull(date, "查询日期不能为空");

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date * 1000L));
        Integer weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;
        List<Map<String, Object>> timeList;
        Long schoolId = null;
        Integer areaCode = null;
        if (UserUtil.isSchoolUser(user)) {
            schoolId = user.getSchoolId();
        } else {
            areaCode = user.getAreaCode();
        }
        timeList = reservationTimeConfigMapper.getAvailableTimeByDate(schoolId, areaCode, weekDay, getDate(date));
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> timeConfig : timeList) {
            int reservationNum =
                getReservationNum(Long.parseLong(timeConfig.get("user_id").toString()), timeConfig.get("start_time").toString(), timeConfig.get("end_time").toString(),
                    getDate(date));
            if (reservationNum < Integer.parseInt(timeConfig.get("interval_num").toString())) {
                if (resultList.stream().noneMatch(map -> map.get("startTime").equals(timeConfig.get("start_time")) && map.get("endTime").equals(timeConfig.get("end_time")))) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("startTime", timeConfig.get("start_time"));
                    map.put("endTime", timeConfig.get("end_time"));
                    resultList.add(map);
                }
            }
        }
        resultList =
            resultList.stream().sorted(Comparator.comparing((Map<String, Object> map) -> map.get("startTime").toString()).thenComparing(map -> map.get("endTime").toString()))
                .collect(Collectors.toList());
        return resultList;
    }

    private void addTimeConfig(Map<Integer, List<Map<String, String>>> timeConfigs, ReservationConfig model) {
        List<ReservationTimeConfig> reservationTimeConfigs = new ArrayList<>();
        timeConfigs.forEach((key, values) -> {
            for (Map<String, String> date : values) {
                ReservationTimeConfig reservationTimeConfig = new ReservationTimeConfig();
                reservationTimeConfig.setConfigId(model.getId());
                reservationTimeConfig.setUserId(model.getUserId());
                reservationTimeConfig.setStartTime(date.get("startTime"));
                reservationTimeConfig.setEndTime(date.get("endTime"));
                reservationTimeConfig.setWeekDay(key);
                reservationTimeConfigs.add(reservationTimeConfig);
            }
        });
        if (reservationTimeConfigs.size() > 0) {
            reservationTimeConfigMapper.insertBatch(reservationTimeConfigs);
        }
    }

    private List<Map<String, Object>> filterTeacherList(List<Map<String, Object>> teacherList, Date date) {
        List<Map<String, Object>> filterList = new ArrayList<>();
        for (Map<String, Object> teacher : teacherList) {
            int reservationNum = getReservationNum(Long.parseLong(teacher.get("id").toString()), teacher.get("start_time").toString(), teacher.get("end_time").toString(), date);
            if (reservationNum < Integer.parseInt(teacher.get("interval_num").toString())) {
                Map<String, Object> map = new HashMap<>();
                if (filterList.stream().noneMatch(m -> m.get("id").equals(teacher.get("id")))) {
                    map.put("id", teacher.get("id"));
                    map.put("name", teacher.get("name"));
                    filterList.add(map);
                }
            }
        }
        return filterList;
    }

    private int getReservationNum(Long userId, String startTime, String endTime, Date date) {
        LambdaQueryWrapper<NewReservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(NewReservation::getPsychiatristId, userId);
        reservationWrapper.eq(NewReservation::getStartTime, startTime);
        reservationWrapper.eq(NewReservation::getEndTime, endTime);
        reservationWrapper.eq(NewReservation::getDate, date);
        reservationWrapper.in(NewReservation::getStatus, new ArrayList<Integer>() {{
            add(0);
            add(1);
            add(2);
        }});
        return reservationMapper.selectList(reservationWrapper).size();
    }

    private Date getDate(Long date) {
        return new Date(date * 1000L);
    }

    private void checkExpireDate(Date expireDate) {
        if (expireDate != null) {
            Assert.isTrue(new Date().before(expireDate), "过期日期不能早于当前日期");
        }
    }

    private void fullTimeConfigCheck(List<Map<String, Object>> result, Long userId) {
        LambdaQueryWrapper<NewReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewReservation::getPsychiatristId, userId);
        wrapper.ge(NewReservation::getDate, getTodayStartDate());
        wrapper.in(NewReservation::getStatus, 0, 1);
        wrapper.orderByAsc(NewReservation::getDate);
        List<NewReservation> list = reservationMapper.selectList(wrapper);
        if (CollUtil.isNotEmpty(list)) {
            for (NewReservation reservation : list) {
                addResult(result, reservation);
            }
        }
    }

    private void updateTimeConfigCheck(List<Map<String, Object>> result, Long userId, ReservationConfigReqVO vo) {
        List<ReservationTimeConfig> timeConfigList = reservationTimeConfigMapper.selectList(new QueryWrapper<ReservationTimeConfig>().eq("user_id", userId));
        buildTimeConfig(vo);
        // 查看是否有记录被删除并且存在预约的
        if (CollUtil.isNotEmpty(timeConfigList)) {
            for (ReservationTimeConfig timeConfig : timeConfigList) {
                if (vo.getTimeConfig().get(timeConfig.getWeekDay()).stream()
                    .noneMatch(map -> map.get("startTime").equals(timeConfig.getStartTime()) && map.get("endTime").equals(timeConfig.getEndTime()))) {
                    LambdaQueryWrapper<NewReservation> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(NewReservation::getPsychiatristId, userId);
                    wrapper.ge(NewReservation::getDate, getTodayStartDate());
                    wrapper.eq(NewReservation::getStartTime, timeConfig.getStartTime());
                    wrapper.eq(NewReservation::getEndTime, timeConfig.getEndTime());
                    wrapper.in(NewReservation::getStatus, 0, 1);
                    List<NewReservation> list = reservationMapper.selectList(wrapper);
                    if (CollUtil.isNotEmpty(list)) {
                        for (NewReservation reservation : list) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(reservation.getDate());
                            int weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;
                            if (weekDay == timeConfig.getWeekDay() && reservation.getStartTime().equals(timeConfig.getStartTime()) && reservation.getEndTime()
                                .equals(timeConfig.getEndTime())) {
                                addResult(result, reservation);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addResult(List<Map<String, Object>> result, NewReservation reservation) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", reservation.getDate().getTime() / 1000);
        map.put("start_time", reservation.getStartTime());
        map.put("end_time", reservation.getEndTime());
        result.add(map);
    }

    private void buildTimeConfig(ReservationConfigReqVO vo) {
        vo.setTimeConfig(vo.getTimeConfig() == null ? new HashMap<>() : vo.getTimeConfig());
        Map<Integer, List<Map<String, String>>> timeConfig = vo.getTimeConfig();
        for (int i = 1; i <= 7; i++) {
            timeConfig.computeIfAbsent(i, k -> new ArrayList<>());
        }
    }

    private Date getTodayStartDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private static String getLatestTime(Calendar cal, int intervalTime) {
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(new Date());
        if (cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH) && cal.get(Calendar.DAY_OF_MONTH) == nowCal.get(
            Calendar.DAY_OF_MONTH)) {
            boolean addHour = intervalTime != 30 || (nowCal.get(Calendar.MINUTE) >= 30);
            int hour = nowCal.get(Calendar.HOUR_OF_DAY);
            String latestHour = hour < 10 ? (hour == 9 ? (addHour ? hour + 1 + "" : "0" + (hour + 1)) : ("0" + (hour + 1))) : (addHour ? hour + 1 : hour) + "";
            String latestMin = intervalTime == 30 ? (nowCal.get(Calendar.MINUTE) < 30 ? "30" : "00") : "00";
            return latestHour + latestMin;
        }
        return null;
    }
}
