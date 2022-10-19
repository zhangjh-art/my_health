package com.cnasoft.health.userservice.util;

import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.feign.AuthFeign;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.StudentAdditionalInfoMapper;
import com.cnasoft.health.userservice.mapper.StudentBaseInfoMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.AREA_NOT_EXISTS;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_NOT_EXISTS;


/**
 * 用户工具类
 *
 * @author cnasoft
 * @date 2020/8/17 14:09
 */
@Slf4j
@Component
public final class UserUtil {

    private UserUtil() {

    }

    /**
     * 字典类型
     */
    public static final String SCHOOL_DEPARTMENT = "SchoolDepartment";
    public static final String SCHOOL_GRADE = "SchoolGrade";
    public static final String DYNAMIC_WARN = "DynamicWarningKeyword";

    private static final int ID_LEN = 15;

    private static String key;

    @Value("${user.password.key}")
    public synchronized void setKey(String key) {
        UserUtil.key = key;
    }

    private static TaskExecutor taskExecutor;

    @Resource
    public synchronized void setTaskExecutor(TaskExecutor taskExecutor) {
        UserUtil.taskExecutor = taskExecutor;
    }

    private static ISysUserService userService;

    @Resource
    public synchronized void setUserService(ISysUserService userService) {
        UserUtil.userService = userService;
    }

    private static AuthFeign authFeign;

    @Resource
    public synchronized void setAuthFeign(AuthFeign authFeign) {
        UserUtil.authFeign = authFeign;
    }

    /**
     * 获取当前登录
     *
     * @return 学校ID
     */
    public static SysUserDTO getCurrentUser() {
        Long userId = SysUserUtil.getHeaderUserId();
        if (userId == 0) {
            throw exception(USER_NOT_EXISTS);
        }

        return userService.findByUserId(userId, false);
    }

    /**
     * 获取当前登录用户的schoolId
     *
     * @return 学校ID
     */
    public static Long getSchoolId() {
        SysUserDTO currentUser = getCurrentUser();
        Long schoolId = currentUser.getSchoolId();
        if (Objects.isNull(schoolId) || schoolId == 0L) {
            throw exception("缺少学校信息");
        }
        return schoolId;
    }

    /**
     * 获取当前登录用户的schoolId
     *
     * @return 学校ID
     */
    public static Long getSchoolIdByDefault() {
        SysUserDTO currentUser = getCurrentUser();
        Long schoolId = currentUser.getSchoolId();
        if (Objects.isNull(schoolId) || schoolId == 0L) {
            return null;
        }
        return schoolId;
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        Long userId = SysUserUtil.getHeaderUserId();
        if (userId == 0L) {
            throw exception("请先登录");
        }
        return userId;
    }

    /**
     * 获取当前登录用户的角色编码
     *
     * @return
     */
    public static String getRoleCode() {
        SysUserDTO currentUser = getCurrentUser();
        return currentUser.getPresetRoleCode();
    }

    /**
     * 获取当前登录用户的区域编码
     *
     * @return 区域ID
     */
    public static Integer getAreaCode() {
        SysUserDTO currentUser = getCurrentUser();
        Integer areaCode = currentUser.getAreaCode();
        if (Objects.isNull(areaCode) || areaCode == 0) {
            throw exception(AREA_NOT_EXISTS);
        }
        return areaCode;
    }

    /**
     * 获取当前登录用户的区域编码
     *
     * @return 区域ID
     */
    public static Integer getAreaCodeByDefault() {
        SysUserDTO currentUser = getCurrentUser();
        Integer areaCode = currentUser.getAreaCode();
        if (Objects.isNull(areaCode) || areaCode == 0) {
            return null;
        }
        return areaCode;
    }

    /**
     * 设置查询条件
     *
     * @param params 请求参数
     * @return Map对象
     */
    public static Map<String, Object> setSearchParams(Map<String, Object> params) {
        if (ObjectUtils.isNotEmpty(params)) {
            if (params.containsKey(Constant.QUERY)) {
                String value = params.get(Constant.QUERY).toString();
                if (StringUtils.isNotBlank(value)) {
                    if (StringUtils.isNumeric(value)) {
                        params.put("number", value);
                    } else {
                        params.put("text", value);
                    }
                }
            }
            if (params.containsKey(Constant.ENABLED)) {
                params.put(Constant.ENABLED, Boolean.valueOf(params.get(Constant.ENABLED).toString()));
            }
        } else {
            params = new HashMap<>(16);
        }
        return params;
    }

    /**
     * 获取数据字典数据
     *
     * @return 部门数据
     */
    public static Map<String, String> getDictData(String dictType) {
        Map<String, String> dictMap = new HashMap<>(16);
        if (StringUtils.isBlank(dictType)) {
            return dictMap;
        }

        List<SysDictDTO> dictData = RedisUtils.getDictData(dictType);
        if (CollectionUtils.isNotEmpty(dictData)) {
            dictMap = dictData.stream().collect(Collectors.toMap(SysDictDTO::getDictName, SysDictDTO::getDictValue, (key1, key2) -> key2));
        }

        return dictMap;
    }

    /**
     * 根据map的value获取map的key
     *
     * @param map
     * @param value
     * @return
     */
    public static String getKey(Map<String, String> map, String value) {
        String key = StringUtils.EMPTY;
        if (!map.containsValue(value)) {
            return key;
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    /**
     * 批量添加或更新数据
     *
     * @param dataList 操作数据
     * @param mapper   操作对象
     */
    public static void saveOrUpdateBatch(List<Object> dataList, SuperMapper mapper) throws Exception {
        List<Object> adds = new ArrayList<>();
        List<Object> updates = new ArrayList<>();
        Set<String> accounts = new HashSet<>();

        for (Object data : dataList) {
            Class<?> aClass = data.getClass();
            Method getIdMethod = aClass.getMethod("getId");
            Object id = getIdMethod.invoke(data);
            if (Objects.isNull(id)) {
                adds.add(data);
            } else {
                updates.add(data);
            }
        }

        int batchCount = Constant.BATCH_COUNT;
        if (adds.size() > 0) {
            int count = adds.size();
            if (count > batchCount) {
                int times = (count + batchCount - 1) / batchCount;

                List<Object> batchList;
                for (int i = 0; i < times; i++) {
                    if (i == times - 1) {
                        batchList = adds.subList(i * batchCount, count);
                    } else {
                        batchList = adds.subList(i * batchCount, (i + 1) * batchCount);
                    }

                    List<Object> finalBatchList = batchList;
                    if ((mapper instanceof ParentMapper) || (mapper instanceof SysUserMapper) ||
                            (mapper instanceof StudentBaseInfoMapper) || (mapper instanceof StudentAdditionalInfoMapper)) {
                        mapper.insertBatch(finalBatchList, key);
                        if (mapper instanceof SysUserMapper) {
                            List<SysUser> studentUsers = (List) finalBatchList;
                            studentUsers.forEach(user -> accounts.addAll(Sets.newHashSet(user.getUsername(), user.getShortId(), user.getMobile())));
                        }
                    } else {
                        mapper.insertBatch(finalBatchList);
                    }
                }

            } else {
                if ((mapper instanceof ParentMapper) || (mapper instanceof SysUserMapper) ||
                        (mapper instanceof StudentBaseInfoMapper) || (mapper instanceof StudentAdditionalInfoMapper)) {
                    mapper.insertBatch(adds, key);
                    if (mapper instanceof SysUserMapper) {
                        List<SysUser> studentUsers = (List) adds;
                        studentUsers.forEach(user -> accounts.addAll(Sets.newHashSet(user.getUsername(), user.getShortId(), user.getMobile())));
                    }
                } else {
                    mapper.insertBatch(adds);
                }
            }
        }

        if (updates.size() > 0) {
            int count = updates.size();
            if (count > batchCount) {
                int times = (count + batchCount - 1) / batchCount;

                List<Object> batchList;
                for (int i = 0; i < times; i++) {
                    if (i == times - 1) {
                        batchList = updates.subList(i * batchCount, count);
                    } else {
                        batchList = updates.subList(i * batchCount, (i + 1) * batchCount);
                    }

                    List<Object> finalBatchList = batchList;
                    if ((mapper instanceof ParentMapper) || (mapper instanceof SysUserMapper) ||
                            (mapper instanceof StudentBaseInfoMapper) || (mapper instanceof StudentAdditionalInfoMapper)) {
                        mapper.updateBatch(finalBatchList, key);
                    } else {
                        mapper.updateBatch(finalBatchList);
                    }
                }
            } else {
                if ((mapper instanceof ParentMapper) || (mapper instanceof SysUserMapper) ||
                        (mapper instanceof StudentBaseInfoMapper) || (mapper instanceof StudentAdditionalInfoMapper)) {
                    mapper.updateBatch(updates, key);
                } else {
                    mapper.updateBatch(updates);
                }
            }
        }

        if (!accounts.isEmpty()) {
            taskExecutor.execute(() -> {
                authFeign.addUserToFilter(accounts);
            });
        }
    }

    /**
     * 通过身份证获取出生日期
     *
     * @param idCardNumber 身份证号
     * @return 出生日期
     */
    public static String getBirthday(String idCardNumber) {
        if (Pattern.matches(TextValidator.REGEX_ID, idCardNumber)) {
            String birthday;
            String year;
            String month;
            String day;
            if (idCardNumber.length() == ID_LEN) {
                //身份证上的年份(15位身份证为1980年前的)
                year = "19" + idCardNumber.substring(6, 8);
                //身份证上的月份
                month = idCardNumber.substring(8, 10);
                //身份证上的日期
                day = idCardNumber.substring(10, 12);
            } else {
                //18位身份证号
                //身份证上的年份
                year = idCardNumber.substring(6).substring(0, 4);
                //身份证上的月份
                month = idCardNumber.substring(10).substring(0, 2);
                //身份证上的日期
                day = idCardNumber.substring(12).substring(0, 2);
            }
            birthday = year + "-" + month + "-" + day;
            return birthday;
        } else {
            return null;
        }
    }

    /**
     * 通过身份证获取性别，0：未知，1：男；2：女
     *
     * @param idCardNumber 身份证号
     * @return 性别
     */
    public static Integer getSex(String idCardNumber) {
        if (Pattern.matches(TextValidator.REGEX_ID, idCardNumber)) {
            int gender;
            char sex;
            if (idCardNumber.length() == ID_LEN) {
                sex = idCardNumber.charAt(idCardNumber.length() - 1);
            } else {
                sex = idCardNumber.charAt(idCardNumber.length() - 2);
            }

            gender = Integer.parseInt(String.valueOf(sex));
            if (gender % 2 == 1) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return 0;
        }
    }

    /**
     * 根据年级码值，计算入学年份
     *
     * @param dictValue 年级码值
     * @return int     入学年份
     */
    public static Integer getAdmissionYear(String dictValue) {
        int step = 0;
        switch (dictValue) {
            case "SG01":
            case "SG02":
            case "SG03":
            case "SG04":
            case "SG05":
            case "SG06":
                step = Integer.parseInt(dictValue.substring(dictValue.length() - 1)) - 1;
                break;
            case "SG07":
            case "SG10":
            case "SG13":
            case "SG17":
                step = 1;
                break;
            case "SG08":
            case "SG11":
            case "SG14":
            case "SG18":
                step = 2;
                break;
            case "SG09":
            case "SG12":
            case "SG15":
            case "SG19":
                step = 3;
                break;
            case "SG16":
                step = 4;
                break;
            default:
                break;
        }

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = 1 + c.get(Calendar.MONTH);
        if (month < 9) {
            step += 1;
        }
        return year - step;
    }

    public static void desensitizedMobile(UserRespVO userRespVO) {
        if (Objects.isNull(userRespVO)) {
            return;
        }
        // 账号名(手机号、身份证号)脱敏
        if (StringUtils.isNotBlank(userRespVO.getUsername())) {
            if (TextValidator.isMobileExact(userRespVO.getUsername())) {
                userRespVO.setUsername(DesensitizedUtil.desensitized(userRespVO.getUsername(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
                userRespVO.setDesensitized(true);
            } else if (TextValidator.isIdCard(userRespVO.getUsername())) {
                userRespVO.setUsername(DesensitizedUtil.desensitized(userRespVO.getUsername(), DesensitizedUtil.DesensitizedType.ID_CARD));
                userRespVO.setDesensitized(true);
            } else {
                userRespVO.setDesensitized(false);
            }
        }
        // 手机号脱敏
        if (StringUtils.isNotBlank(userRespVO.getMobile())) {
            userRespVO.setMobile(DesensitizedUtil.desensitized(userRespVO.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
        }
    }

    public static boolean isSchoolUser(SysUserDTO user) {
        String roleCode = user.getPresetRoleCode();
        if (RoleEnum.student.getValue().equals(roleCode) || RoleEnum.parents.getValue().equals(roleCode)
                || RoleEnum.school_staff.getValue().equals(roleCode) || RoleEnum.school_head_teacher.getValue().equals(roleCode)
                || RoleEnum.school_psycho_teacher.getValue().equals(roleCode) || RoleEnum.school_admin.getValue().equals(roleCode)
                || RoleEnum.school_leader.getValue().equals(roleCode)) {
            return true;
        }
        return false;
    }
}
