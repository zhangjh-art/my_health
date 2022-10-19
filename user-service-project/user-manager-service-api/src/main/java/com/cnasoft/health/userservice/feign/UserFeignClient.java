package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.dto.AreaStaffDTO;
import com.cnasoft.health.common.dto.AreaTeacherDTO;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.MessageDTO;
import com.cnasoft.health.common.dto.ParentDTO;
import com.cnasoft.health.common.dto.ParentStudentDTO;
import com.cnasoft.health.common.dto.SchoolStaffDTO;
import com.cnasoft.health.common.dto.SchoolTeacherDTO;
import com.cnasoft.health.common.dto.SchoolTeacherStaffDTO;
import com.cnasoft.health.common.dto.StudentBaseInfoDTO;
import com.cnasoft.health.common.dto.StudentDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.constant.UserConstant;
import com.cnasoft.health.userservice.feign.dto.ConsultationReportRespVO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/12 16:26
 */
@FeignClient(contextId = "userClient", name = UserConstant.FEIGN_USER, decode404 = true)
public interface UserFeignClient {

    /**
     * 获取用户总量
     *
     * @return Long
     */
    @GetMapping(value = "/user/count")
    CommonResult<Long> findUserCount();

    /**
     * 获取用户
     *
     * @param limit
     * @param offset
     * @return
     */
    @GetMapping(value = "/user/findUsernameAndMobileOrShortId")
    CommonResult<List<String>> findUsernameAndMobileOrShortId(@RequestParam("limit") Integer limit, @RequestParam("offset") Integer offset);

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    @GetMapping(value = "/user/info/username")
    CommonResult<SysUserDTO> findUserByUsername(@RequestParam("username") String username);

    /**
     * 根据用户id查询用户
     *
     * @param id 用户id
     * @return
     */
    @GetMapping(value = "/user/info")
    CommonResult<SysUserDTO> findUserById(@RequestParam("id") Long id);

    /**
     * 根据用户id查询用户角色
     *
     * @param id 用户id
     * @return 角色编码
     */
    @GetMapping(value = "/user/roleCode")
    CommonResult<String> findRoleCodeById(@RequestParam("id") Long id);

    /**
     * 根据用户名或电话查询用户
     *
     * @param query 查询条件
     * @return
     */
    @GetMapping(value = "/user/findUserByUsernameOrMobileOrShortId")
    CommonResult<List<SysUserDTO>> findUserByUsernameOrMobileOrShortId(@RequestParam("query") String query);

    /**
     * 根据手机号查询测试管理员用户信息
     *
     * @param query 查询条件
     * @return
     */
    @GetMapping(value = "/user/findUserByUsernameWithTestManager")
    CommonResult<SysUserDTO> findUserByUsernameWithTestManager(@RequestParam("query") String query);

    /**
     * 根据手机号验证码查找用户并校验验证码
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return
     */
    @GetMapping(value = "/user/login-info-mobile")
    CommonResult<List<SysUserDTO>> findLoginUserByMobile(@RequestParam("mobile") String mobile, @RequestParam("captcha") String captcha);

    /**
     * 更改短信验证码使用状态
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return
     */
    @PutMapping(value = "/user/updatesSmsRecordUsedStatus")
    CommonResult<Boolean> updatesSmsRecordUsedStatus(@RequestParam("mobile") String mobile, @RequestParam("captcha") String captcha);

    /**
     * 更新家长确认状态及激活状态
     *
     * @param userId 用户id
     * @return
     */
    @PutMapping(value = "/parent/updateConfirmAndActiveStatus")
    CommonResult<Object> updateConfirmAndActiveStatus(@RequestParam("userId") Long userId);

    /**
     * 发送未读消息
     *
     * @param message message
     * @return null
     */
    @PostMapping(value = "/user/send-message")
    CommonResult<Object> sendMessage(@RequestBody MessageDTO message);

    /**
     * 根据type获取数据字典码值
     *
     * @param type 数据字典码值类型
     * @return List<SysDictDTO>
     */
    @GetMapping("/dict/data/list")
    CommonResult<List<SysDictDTO>> getDictDataList(@RequestParam("type") String type);

    /**
     * 根据type获取数据字典码值（查询数据库，不取缓存数据）
     *
     * @param type
     * @return
     */
    @GetMapping("/dict/data/listNoCache")
    CommonResult<List<SysDictDTO>> getDictDataListNocache(@RequestParam("type") String type);

    /**
     * 区域心里教研员/校心理教研员是否有查看该用户测评结果的数据权限
     *
     * @param taskUserId 测评记录的用户id
     * @return
     */
    @GetMapping("/user/taskInScope")
    CommonResult<Boolean> taskInScope(@RequestParam("taskUserId") Long taskUserId);

    /**
     * 根据用户id和学校id判断用户是否存在
     *
     * @param userId   用户id
     * @param schoolId 学校id
     * @return 是否存在
     */
    @GetMapping(value = "/user/checkExistsUserByUserIdAndSchoolId")
    CommonResult<Boolean> checkExistsUserByUserIdAndSchoolId(@RequestParam("userId") Long userId, @RequestParam("schoolId") Long schoolId);

    /**
     * 根据用户id和区域编码判断用户是否存在
     *
     * @param userId   用户id
     * @param areaCode 区域编码
     * @return 是否存在
     */
    @GetMapping(value = "/user/checkExistsUserByUserIdAndAreaCode")
    CommonResult<Boolean> checkExistsUserByUserIdAndAreaCode(@RequestParam("userId") Long userId, @RequestParam("areaCode") Integer areaCode);

    /**
     * 获取班主任管理的班级信息
     *
     * @return 班级信息
     */
    @GetMapping("/class/getListByHeaderTeacher")
    CommonResult<List<ClazzDTO>> getListByHeaderTeacher();

    /**
     * 根据指定班级id列表获取班级信息
     *
     * @param ids 班级id列表
     * @return
     */
    @GetMapping("/class/list/server")
    CommonResult<List<ClazzDTO>> getClazzListByIds(@RequestBody Set<Long> ids);

    /**
     * 判断班主任管理的班级中是否有该学生
     *
     * @param headerTeacherUserId 班主任用户id
     * @param studentUserId       学生用户id
     * @return 是否存在该学生
     */
    @GetMapping("/class/check/headerTeacherWithStudent")
    CommonResult<Boolean> checkHeaderTeacherWithStudent(@RequestParam("headerTeacherUserId") Long headerTeacherUserId, @RequestParam("studentUserId") Long studentUserId);

    /**
     * 获取区域可用状态
     *
     * @param areaCode 区域编码
     * @return 可用状态
     */
    @GetMapping("/area/getAreaAvailableStatus")
    CommonResult<Boolean> getAreaAvailableStatus(@RequestParam("areaCode") Integer areaCode);

    /**
     * 获取区域信息
     *
     * @param areaCode 区域编码
     * @return 区域数据
     */
    @GetMapping("/area/getArea")
    CommonResult<SysAreaDTO> getArea(@RequestParam("areaCode") Integer areaCode);

    /**
     * 根据用户id列表查询学生基础信息
     *
     * @param userIds 用户id列表
     * @return
     */
    @GetMapping("/student/list/ids")
    CommonResult<List<StudentBaseInfoDTO>> getStudentListByUserIds(@RequestBody Set<Long> userIds);

    /**
     * 根据姓名、学号、年级、班级、测试管理员用户id查询学生用户id列表
     *
     * @param params 查询条件
     * @return 学生用户id列表
     */
    @GetMapping("/student/list/query")
    CommonResult<List<Long>> getUserIdsByQuery(@RequestParam("params") Map<String, Object> params);

    /**
     * 根据姓名、角色、区域编码、学校ID 查询学生用户id列表
     * @param params
     * @return
     */
    @GetMapping("/user/list/query")
    CommonResult<List<Long>> getRoleUserIdsByQuery(@RequestParam("params") Map<String, Object> params);

    /**
     * 根据姓名查询学生用户id列表
     *
     * @param name 姓名
     * @return 学生用户id列表
     */
    @GetMapping("/student/list/name")
    CommonResult<List<Long>> getUserIdsByName(@RequestParam("name") String name);

    /**
     * 根据id获取动态详情
     *
     * @param id 动态id
     * @return 心情动态详情
     */
    @PostMapping("/userMood/detail")
    CommonResult<UserDynamicDTO> getUserDynamic(@RequestParam("id") Long id);

    /**
     * 根据预约id查询预约报告
     *
     * @param reservationId
     * @return
     */
    @GetMapping("/consultationReport/get")
    CommonResult<ConsultationReportRespVO> getConsultationReport(@RequestParam("reservationId") Long reservationId);

    /**
     * 获取用户增量统计数据
     *
     * @return
     */
    @GetMapping("/user/statistics")
    CommonResult<Map<String, List<Map<String, Object>>>> getUserStatistics();

    /**
     * 获取某个学校的学生/家长/教职工的数量
     *
     * @return
     */
    @GetMapping("/user/school/count")
    CommonResult<Integer> getUserCountBySchoolId(@RequestParam("schoolId") Long schoolId, @RequestParam("userRoleType") Integer userRoleType);

    /**
     * 获取某个区域的学生/家长/教职工/区域职工的数量
     *
     * @return
     */
    @GetMapping("/user/area/count")
    CommonResult<Integer> getUserCountByAreaCode(@RequestParam("areaCode") String areaCode, @RequestParam("userRoleType") Integer userRoleType);

    /**
     * 获取学校各类人员数量
     *
     * @return
     */
    @GetMapping("/user/schools/nums")
    CommonResult<Map<String, List<Map<String, Object>>>> getTotalUserNumOfSchool(@RequestParam("schoolId") Long schoolId);

    /**
     * 获取区域下 学校各类人员数量  区域人员数量
     *
     * @return
     */
    @GetMapping("/user/area/nums")
    CommonResult<Map<String, List<Map<String, Object>>>> getTotalUserNumOfArea(@RequestParam("areaCode") String areaCode, @RequestParam("wholeAreaCode") Long wholeAreaCode);

    /**
     * 获取日志信息
     *
     * @param logId 日志id
     * @return 日志信息
     */
    @GetMapping("/log/getLog")
    CommonResult<Object> getLogById(@RequestParam("logId") Long logId);

    /**
     * 根据学校id和年级code获取所有班级列表
     *
     * @param schoolId 学校id
     * @param grade    年级编码
     * @return 班级列表
     */
    @GetMapping("/class/getClazzBySchoolIdAndGrade")
    CommonResult<List<Long>> getClazzBySchoolIdAndGrade(@RequestParam("schoolId") Long schoolId, @RequestParam("grade") String grade);

    /**
     * 查询用户姓名
     *
     * @param userId 用户id
     * @return 用户姓名
     */
    @GetMapping("/user/findNameById")
    CommonResult<String> findNameById(@RequestParam("userId") Long userId);

    /**
     * 根据id列表查询用户名称(name)
     *
     * @param userIds 用户id列表
     * @return 用户信息
     */
    @PostMapping("/user/findNameByIds")
    CommonResult<List<CommonDTO>> findNameByIds(@RequestBody Set<Long> userIds);

    /**
     * 根据条件查询查询家长用户id
     *
     * @param params 查询条件
     * @return 家长用户id列表
     */
    @PostMapping(value = "/parent/getParentUserIdByParams")
    CommonResult<List<Long>> getParentUserIdByParams(@RequestBody Map<String, Object> params);

    /**
     * 根据条件查询学生用户id
     *
     * @param params 查询条件
     * @return 学生用户id
     */
    @PostMapping(value = "/student/getStudentUserIdByParams")
    public CommonResult<List<Long>> getStudentUserIdByParams(@RequestBody Map<String, Object> params);

    /**
     * 根据区域code查找区域承接任务的心理教研员的用户id
     *
     * @param areaCode 区域编码
     * @return 用户id
     */
    @GetMapping("/areaTeacher/findTaskHandlerIdByAreaCode")
    public CommonResult<Long> findTaskHandlerIdByAreaCode(@RequestParam("areaCode") Integer areaCode);

    /**
     * 根据学校id查找承接任务的学校心理老师的用户id
     *
     * @param schoolId 学校id
     * @return 用户id
     */
    @GetMapping("/schoolTeacher/findTaskHandlerIdBySchoolId")
    public CommonResult<Long> findTaskHandlerIdBySchoolId(@RequestParam("schoolId") Long schoolId);

    /**
     * 根据查询条件获取区域用户id列表
     *
     * @param params 查询条件
     * @return 区域用户id列表
     */
    @PostMapping("/user/getAreaUserIdByParams")
    public CommonResult<List<Long>> getAreaUserIdByParams(@RequestBody Map<String, Object> params);

    /**
     * 根据学校和班级查询学生用户id和班级id
     *
     * @param schoolId 学校id
     * @param clazzIds 班级id列表
     * @return 用户id和班级id列表
     */
    @GetMapping(value = "/student/getStudentUserIdClazzIdBySchoolAndClass")
    public CommonResult<List<UserClazzVO>> getStudentUserIdClazzIdBySchoolAndClass(@RequestParam("schoolId") Long schoolId, @RequestParam("clazzIds") List<Long> clazzIds);

    /**
     * 根据学校id和部门code列表获取教职工（包含心理老师）用户id列表
     *
     * @param schoolId        学校i
     * @param departmentCodes 部门列表
     * @return 用户id列表
     */
    @GetMapping("/schoolStaff/getSchoolStaffUserIdBySchoolAndDepartmentCode")
    public CommonResult<List<UserClazzVO>> getSchoolStaffUserIdBySchoolAndDepartmentCode(@RequestParam("schoolId") Long schoolId,
        @RequestParam("departmentCodes") List<String> departmentCodes);

    /**
     * 根据用户id查询学生基本信息
     *
     * @param userIds 用户id
     * @return 学生信息
     */
    @PostMapping(value = "/student/findStudentListByIds")
    public CommonResult<List<StudentDTO>> findStudentListByIds(@RequestBody Set<Long> userIds);

    /**
     * 获取学生的年级和身份证号
     *
     * @param userId 用户id
     * @return 年级和身份证号
     */
    @GetMapping(value = "/student/findStudentGradeAndIDNumber")
    public CommonResult<StudentDTO> findStudentGradeAndIDNumber(@RequestParam("userId") Long userId);

    /**
     * 根据用户id查询学生基本信息
     *
     * @param userId 用户id
     * @return 学生信息
     */
    @GetMapping(value = "/student/findStudentInfo")
    public CommonResult<StudentDTO> findStudentInfo(@RequestParam("userId") Long userId);

    /**
     * 根据用户id查询家长基本信息及子女信息
     *
     * @param userId 用户id
     * @return 家长信息
     */
    @GetMapping(value = "/parent/findParentInfo")
    public CommonResult<ParentStudentDTO> findParentInfo(@RequestParam("userId") Long userId);

    /**
     * 获取心理老师的学校id
     *
     * @param userId 用户id
     * @return 学校id
     */
    @GetMapping("/schoolTeacher/getSchoolPsychoTeacherSchoolId")
    public CommonResult<Long> getSchoolPsychoTeacherSchoolId(@RequestParam("userId") Long userId);

    /**
     * 根据姓名模糊查询所有用户(未删除且已审核的)的userId列表
     *
     * @param name      姓名
     * @param roleCodes 角色编码
     * @return 用户id列表
     */
    @GetMapping("/user/findUserIdListByName")
    CommonResult<List<Long>> findUserIdListByName(@RequestParam("name") String name, @RequestParam("roleCodes") List<String> roleCodes);

    /**
     * 根据用户id列表查询家长基本信息及子女姓名
     *
     * @param userIds 用户id列表
     * @return 家长信息
     */
    @PostMapping(value = "/parent/findParentList")
    public CommonResult<List<ParentDTO>> findParentList(@RequestBody Set<Long> userIds);

    /**
     * 根据用户id获取校心理老师数据
     *
     * @param userId 用户id
     * @return 校心理老师数据
     */
    @GetMapping("/schoolTeacher/findSchoolTeacherInfo")
    public CommonResult<SchoolTeacherDTO> findSchoolTeacherInfo(@RequestParam("userId") Long userId);

    /**
     * 根据用户id获取校教职工数据
     *
     * @param userId 用户id
     * @return 校教职工数据
     */
    @GetMapping("/schoolStaff/findSchoolStaffInfo")
    public CommonResult<SchoolStaffDTO> findSchoolStaffInfo(@RequestParam("userId") Long userId);

    /**
     * 根据用户id查询校心理老师和教职工数据
     *
     * @param userIds 用户id列表
     * @return 校心理老师和教职工数据
     */
    @PostMapping("/schoolStaff/findSchoolTeacherStaffList")
    public CommonResult<List<SchoolTeacherStaffDTO>> findSchoolTeacherStaffList(@RequestBody Set<Long> userIds);

    /**
     * 获取区域职员信息
     *
     * @param userId 用户id
     * @return 区域职员信息
     */
    @GetMapping("/areaStaff/findAreaStaffInfo")
    public CommonResult<AreaStaffDTO> findAreaStaffInfo(@RequestParam("userId") Long userId);

    /**
     * 根据用户id查询区域心理教研员信息
     *
     * @param userId 用户id
     * @return 区域心理教研员信息
     */
    @GetMapping("/areaTeacher/findAreaTeacherInfo")
    public CommonResult<AreaTeacherDTO> findAreaTeacherInfo(@RequestParam("userId") Long userId);

    /**
     * 查询校心理老师数据
     *
     * @param schoolId 学校id
     * @return 校心理老师数据
     */
    @GetMapping("/schoolTeacher/getSchoolPsychoTeacher")
    public CommonResult<List<CommonDTO>> getSchoolPsychoTeacher(@RequestParam("schoolId") Long schoolId);

    /**
     * 查询区域心理教研员数据
     *
     * @param areaCode 区域编码
     * @return 区域心理教研员数据
     */
    @GetMapping("/areaTeacher/getAreaPsychoTeacher")
    public CommonResult<List<CommonDTO>> getAreaPsychoTeacher(@RequestParam("areaCode") Integer areaCode);

    /**
     * 根据家长用户id查询学生用户id列表
     *
     * @param userId 家长用户id
     * @return 学生用户id列表
     */
    @GetMapping(value = "/student/findStudentUserIdByParentUserId")
    public CommonResult<List<Long>> findStudentUserIdByParentUserId(@RequestParam("userId") Long userId);
}