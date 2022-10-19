package com.cnasoft.health.userservice.controller;

import cn.hutool.http.HttpStatus;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveRecord;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.MessageDTO;
import com.cnasoft.health.common.dto.RegionStaffInfoDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.feign.dto.AreaManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.ForgetPasswordVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserAuthorityReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateMobileReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.feign.dto.TestManagerReqVO;
import com.cnasoft.health.userservice.feign.dto.UpdatePasswordReqVO;
import com.cnasoft.health.userservice.feign.dto.UpgradeDTO;
import com.cnasoft.health.userservice.model.SchoolManager;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.error;
import static com.cnasoft.health.common.vo.CommonResult.success;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.COMMON_MESSAGE;

/**
 * 用户接口
 *
 * @author cnasoft
 * @date 2020/8/12 20:23
 */
@Slf4j
@RestController
@Api(tags = "用户模块API")
public class SysUserController {
    @Resource
    private ISysUserService sysUserService;

    @GetMapping("/user/list")
    @ApiOperation(value = "查询用户列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "ID/姓名/手机号", dataType = "String"),
        @ApiImplicitParam(name = "areaCode", value = "区域编号", dataType = "Integer"), @ApiImplicitParam(name = "roleCode", value = "角色编码", dataType = "String"),
        @ApiImplicitParam(name = "schoolId", value = "学校id", dataType = "Integer"), @ApiImplicitParam(name = "areaType", value = "区域类型", dataType = "Integer"),
        @ApiImplicitParam(name = "enabled", value = "启用/禁用", dataType = "Boolean"),
        @ApiImplicitParam(name = "approveStatus", value = "审核状态：0：待审核；1：已通过；2：已拒绝", dataType = "Boolean")})
    public CommonResult<PageResult<SysUserDTO>> findUsers(@RequestParam Map<String, Object> params) {
        return success(sysUserService.findUsers(params));
    }

    @GetMapping("/user/test/list")
    @ApiOperation(value = "查询测试管理员用户列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"), @ApiImplicitParam(name = "query", value = "ID/姓名/手机号", dataType = "String"),
        @ApiImplicitParam(name = "areaCode", value = "区域编号", dataType = "Integer"), @ApiImplicitParam(name = "schoolId", value = "学校id", dataType = "Integer"),
        @ApiImplicitParam(name = "enabled", value = "启用/禁用", dataType = "Boolean"),
        @ApiImplicitParam(name = "approveStatus", value = "审核状态：0：待审核；1：已通过；2：已拒绝", dataType = "Boolean")})
    public CommonResult<PageResult<SysUserDTO>> findTestUsers(@RequestParam Map<String, Object> params) {
        return success(sysUserService.findTestUsers(params));
    }

    @GetMapping("/user/info")
    @ApiOperation(value = "根据id查询用户信息")
    public CommonResult<SysUserDTO> findUserById(@RequestParam Long id) {
        return success(sysUserService.findByUserId(id, true));
    }

    @GetMapping("/user/roleCode")
    @ApiOperation(value = "根据id查询用户角色")
    public CommonResult<String> findRoleCodeById(@RequestParam Long id) {
        return success(sysUserService.findRoleCodeById(id));
    }

    @PostMapping(value = "/user/school/manager/info")
    @ApiOperation(value = "添加校级管理员")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_SCHOOL_MANAGER)
    public CommonResult<Object> saveSchoolManager(@RequestBody @Validated SchoolManagerCreateReqVO managerCreateReqVO) throws Exception {
        SchoolManager schoolManager = sysUserService.saveSchoolManager(managerCreateReqVO);
        return schoolManager == null ? error(COMMON_MESSAGE.getMessage()) : success(schoolManager);
    }

    @PutMapping(value = "/user/school/manager/info")
    @ApiOperation(value = "修改校级管理员")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_SCHOOL_MANAGER)
    public CommonResult<Object> updateSchoolManager(@RequestBody @Validated SchoolManagerUpdateReqVO managerUpdateReqVO) throws Exception {
        sysUserService.updateSchoolManager(managerUpdateReqVO);
        return success();
    }

    @DeleteMapping(value = "/user/school/manager/delete")
    @ApiOperation(value = "删除校级管理员")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_SCHOOL_MANAGER)
    public CommonResult<Object> deleteSchoolManager(@RequestParam("ids") Set<Long> ids) {
        sysUserService.deleteSchoolManager(ids);
        return success();
    }

    @PostMapping(value = "/user/test/manager/info")
    @ApiOperation(value = "添加测试管理员")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_TEST_MANAGER)
    public CommonResult<Object> saveTestManager(@RequestBody @Validated TestManagerReqVO testManagerReqVO) throws Exception {
        TestManagerReqVO reqVO = sysUserService.saveTestManager(testManagerReqVO);
        return reqVO == null ? error(COMMON_MESSAGE.getMessage()) : success(testManagerReqVO);
    }

    @PutMapping(value = "/user/test/manager/info")
    @ApiOperation(value = "修改测试管理员")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_TEST_MANAGER)
    public CommonResult<Object> updateTestManager(@RequestBody @Validated(TestManagerReqVO.Update.class) TestManagerReqVO testManagerReqVO) throws Exception {
        TestManagerReqVO reqVO = sysUserService.saveTestManager(testManagerReqVO);
        return reqVO == null ? error(COMMON_MESSAGE.getMessage()) : success(testManagerReqVO);
    }

    @DeleteMapping(value = "/user/test/manager/info")
    @ApiOperation(value = "删除测试管理员")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_TEST_MANAGER)
    public CommonResult<Object> deleteTestManager(@RequestParam("ids") Set<Long> ids) {
        sysUserService.deleteTestManager(ids);
        return success();
    }

    @PostMapping(value = "/user/first/manager/info")
    @ApiOperation(value = "添加一级管理员")
    public CommonResult<Object> saveFirstLevelManager(@RequestBody @Validated SysUserCreateReqVO createReqVO) throws Exception {
        return sysUserService.saveFirstLevelManager(createReqVO) ? success() : error(COMMON_MESSAGE.getMessage());
    }

    @PostMapping(value = "/user/second/manager/info")
    @ApiOperation(value = "添加二级管理员")
    public CommonResult<Object> saveSecondLevelManager(@RequestBody @Validated SysUserCreateReqVO createReqVO) throws Exception {
        return sysUserService.saveSecondLevelManager(createReqVO) ? success() : error(COMMON_MESSAGE.getMessage());
    }

    @PostMapping(value = "/user/area/manager/info")
    @ApiOperation(value = "添加区域管理员")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_AREA_MANAGER)
    public CommonResult<Object> saveAreaManager(@RequestBody @Validated AreaManagerCreateReqVO managerCreateReqVO) throws Exception {
        SysUser sysUser = sysUserService.saveAreaManager(managerCreateReqVO);
        return sysUser == null ? error(COMMON_MESSAGE.getMessage()) : success(sysUser);
    }

    @PutMapping(value = "/user/area/manager/info")
    @ApiOperation(value = "修改区域管理员")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_AREA_MANAGER)
    public CommonResult<Object> updateAreaManager(@RequestBody @Validated AreaManagerUpdateReqVO managerCreateReqVO) throws Exception {
        return sysUserService.updateAreaManager(managerCreateReqVO) ? success() : error(COMMON_MESSAGE.getMessage());
    }

    @DeleteMapping(value = "/user/area/manager/delete")
    @ApiOperation(value = "删除区域管理员")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_AREA_MANAGER)
    public CommonResult<Object> deleteAreaManager(@RequestParam("ids") Set<Long> ids) {
        sysUserService.deleteAreaManager(ids);
        return success();
    }

    @PutMapping(value = "/user/info")
    @ApiOperation(value = "修改用户信息")
    public CommonResult<Object> update(@RequestBody @Validated SysUserUpdateReqVO sysUserUpdateReqVO) throws Exception {
        sysUserService.updateUserBaseInfo(sysUserUpdateReqVO);
        return success();
    }

    @PutMapping(value = "/user/info/updateInfo")
    @ApiOperation(value = "修改当前登录用户的用户信息")
    public CommonResult<Object> updateUserInfo(@RequestBody @Validated SysUserReqVO vo) {
        sysUserService.updateUserInfo(vo);
        return success();
    }

    @PutMapping(value = "/user/h5/updateMobile")
    @ApiOperation(value = "h5端修改用户手机")
    public CommonResult<Object> h5UpdateUserMobile(@RequestBody @Validated SysUserUpdateMobileReqVO vo) throws Exception {
        sysUserService.h5UpdateUserMobile(vo);
        return success();
    }

    @PutMapping(value = "/user/h5/updateUserInfo")
    @ApiOperation(value = "h5端修改用户信息(性别、名字)")
    public CommonResult<Object> h5UpdateUserInfo(@RequestBody @Validated SysUserReqVO vo) throws Exception {
        sysUserService.h5UpdateUserInfo(vo);
        return success();
    }

    @PostMapping(value = "/user/h5/uploadFile")
    @ApiOperation(value = "h5端提供给家长学生使用的意见反馈专用文件上传接口")
    public CommonResult<List<String>> h5FileUpload(@RequestBody @Valid @NotNull(message = "上传文件不可为空") MultipartFile[] file) {
        return sysUserService.h5FileUpload(file);
    }

    @DeleteMapping(value = "/user/info")
    @ApiOperation(value = "删除用户")
    public CommonResult<List<BatchOperationTipDTO>> delete(@RequestParam Set<Long> ids) {
        return success(sysUserService.delUser(ids));
    }

    @PutMapping("/user/role")
    @ApiOperation(value = "给用户分配角色")
    public CommonResult<Object> setRoleToUser(@RequestParam Long id, @RequestBody List<String> roleCodes) {
        sysUserService.setRoleToUser(id, roleCodes);
        return success();
    }

    @PutMapping("/user/authority")
    @ApiOperation(value = "给用户分配权限")
    public CommonResult<Object> setAuthorityToUser(@RequestParam Long id, @RequestBody Set<SysUserAuthorityReqVO> userAuthorityReqVOS) {
        sysUserService.setAuthorityToUser(id, userAuthorityReqVOS);
        return success();
    }

    @GetMapping("/user/authority")
    @ApiOperation(value = "查询用户权限")
    public CommonResult<List<SysAuthority>> getUserAuthority(@RequestParam Long id) {
        return success(sysUserService.findAuthoritiesByUserId(id));
    }

    @PutMapping("/user/enabled")
    @ApiOperation(value = "修改用户状态-无需审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "userId", value = "用户id", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "enabled", value = "是否启用", required = true, dataType = "Boolean")})
    public CommonResult<Object> updateEnabled(@RequestParam Long userId, @RequestParam Boolean enabled) {
        sysUserService.updateEnabled(userId, enabled);
        return success();
    }

    @PutMapping("/user/approve/enabled")
    @ApiOperation(value = "修改用户状态-需要审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "userId", value = "用户id", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "enabled", value = "是否启用", required = true, dataType = "Boolean")})
    @ApproveRecord(operation = ApproveOperation.ENABLE_DISABLE, handleServiceNames = {ApproveBeanName.APPROVE_SCHOOL_MANAGER, ApproveBeanName.APPROVE_AREA_MANAGER,
        ApproveBeanName.APPROVE_TEST_MANAGER})
    public CommonResult<Object> updateEnabledNeedApprove(@RequestParam Long userId, @RequestParam Boolean enabled) {
        sysUserService.updateEnabled(userId, enabled);
        return success();
    }

    @PutMapping(value = "/user/updatePassword")
    @ApiOperation(value = "修改密码")
    public CommonResult<Object> updatePassword(@RequestBody @Validated UpdatePasswordReqVO updatePasswordReqVO) {
        sysUserService.updatePassword(updatePasswordReqVO);
        return success();
    }

    @PutMapping(value = "/user/resetPassword")
    @ApiOperation(value = "重置密码,默认密码1234567890")
    public CommonResult<Object> resetPassword(@RequestParam Long userId) {
        sysUserService.resetPassword(userId);
        return success();
    }

    @PostMapping(value = "/user/forgetPassword")
    @ApiOperation(value = "忘记密码")
    public CommonResult<Object> forgetPassword(@RequestBody @Validated ForgetPasswordVO forgetPassword) {
        sysUserService.forgetPassword(forgetPassword);
        return success();
    }

    @GetMapping(value = "/user/login-info-mobile")
    @ApiOperation(value = "根据手机号验证码查找用户并校验验证码")
    public CommonResult<List<SysUserDTO>> findLoginUserByMobile(@RequestParam("mobile") String mobile, @RequestParam("captcha") String captcha,
        @RequestParam(value = "h5", required = false) boolean h5) {
        // h5只能返回家长和学生用户，pc可以返回所有用户，兼容以前接口做此区分
        return success(sysUserService.findLoginUserByMobile(mobile, captcha, h5));
    }

    @PutMapping(value = "/user/updatesSmsRecordUsedStatus")
    @ApiOperation(value = "更改短信验证码使用状态")
    public CommonResult<Boolean> updatesSmsRecordUsedStatus(@RequestParam("mobile") String mobile, @RequestParam("captcha") String captcha) {
        return success(sysUserService.updatesSmsRecordUsedStatus(mobile, captcha));
    }

    @GetMapping(value = "/user/findUserByUsernameOrMobileOrShortId")
    @ApiOperation(value = "根据用户名或者手机号查询用户-b端鉴权服务使用")
    public CommonResult<List<SysUserDTO>> findUserByUsernameOrMobileOrShortId(@RequestParam("query") String query) {
        return success(sysUserService.findUserByUsernameOrMobileOrShortId(query));
    }

    @GetMapping(value = "/user/findUserByUsernameWithTestManager")
    @ApiOperation(value = "根据手机号查询测试管理员用户信息-b端鉴权服务使用")
    public CommonResult<SysUserDTO> findUserByUsernameWithTestManager(@RequestParam("query") String query) {
        return success(sysUserService.findUserByUsernameWithTestManager(query));
    }

    @GetMapping(value = "/user/info/username")
    @ApiOperation(value = "根据用户名查询用户信息")
    public CommonResult<SysUserDTO> findUserByUsername(@RequestParam String username) {
        return success();
    }

    @GetMapping("/user/info/current")
    @ApiOperation(value = "查询当前用户信息")
    public CommonResult<SysUserDTO> findCurrent() {
        Long userId = SysUserUtil.getHeaderUserId();
        if (userId == 0L) {
            return error(HttpStatus.HTTP_BAD_REQUEST, "请重新登录");
        }

        SysUserDTO sysUserDTO = sysUserService.findByUserId(userId, true);
        sysUserDTO.setPassword(null);
        sysUserDTO.setPermissions(null);
        SysUserUtil.desensitizedUserInfo(sysUserDTO);
        return success(sysUserDTO);
    }

    /**
     * 发送未读消息
     *
     * @param message message
     * @return null
     */
    @PostMapping(value = "/user/send-message")
    CommonResult<Object> sendMessage(@RequestBody MessageDTO message) {
        sysUserService.sendMessage(message);
        return success();
    }

    @PostMapping("/user/getTaskUsers")
    @ApiOperation(value = "发起任务/确认任务时选择用户")
    public CommonResult<PageResult<TaskUserResVO>> getTaskUsers(@RequestBody @Validated TaskUserReqVO query) throws Exception {
        return success(sysUserService.getTaskUsers(query));
    }

    @GetMapping("/user/getRegionStaffInfo")
    @ApiOperation(value = "获取区域职员的部门岗位信息")
    public CommonResult<RegionStaffInfoDTO> getRegionStaffInfo(@RequestParam("userId") Long userId) {
        return success(sysUserService.getRegionStaffInfo(userId));
    }

    /**
     * 区心理教研员/校心理老师是否有查看该用户测评结果的数据权限
     *
     * @param taskUserId 测评记录的用户id
     * @return 返回结果
     */
    @GetMapping("/user/taskInScope")
    CommonResult<Boolean> taskInScope(@RequestParam(value = "taskUserId") Long taskUserId) {
        return success(sysUserService.taskInScope(taskUserId));
    }

    /**
     * 获取用户总量-b端 供鉴权模块初始化使用
     *
     * @return Long
     */
    @GetMapping(value = "/user/count")
    @ApiOperation(value = "获取用户总量-b端")
    CommonResult<Long> findUserCount() {
        return success(sysUserService.findUserCount());
    }

    /**
     * 获取用户username和mobile的列表-b端 供鉴权模块初始化使用
     *
     * @return List<String>
     */
    @GetMapping(value = "/user/findUsernameAndMobileOrShortId")
    @ApiOperation(value = "获取用户username和mobile的列表-b端")
    CommonResult<List<String>> findUsernameAndMobile(@RequestParam("limit") Integer limit, @RequestParam("offset") Integer offset) {
        return success(sysUserService.findUsernameAndMobile(limit, offset));
    }

    @GetMapping(value = "/user/test/manage/school")
    @ApiOperation(value = "获取测试管理员管理的学校数据")
    CommonResult<List<SchoolDTO>> findSchoolByTestManager() {
        return success(sysUserService.findSchoolByTestManager());
    }

    @PutMapping(value = "/user/info/headImg")
    @ApiOperation(value = "更新当前用户头像")
    CommonResult<String> updateHeadImg(@RequestBody String headImgUrl) {
        headImgUrl = headImgUrl.replace("\"", "");
        return success(sysUserService.updateHeadImg(headImgUrl));
    }

    @GetMapping(value = "/user/checkExistsUserByUserIdAndSchoolId")
    @ApiOperation(value = "根据用户id和学校id判断用户是否存在")
    CommonResult<Boolean> checkExistsUserByUserIdAndSchoolId(@RequestParam Long userId, @RequestParam Long schoolId) {
        return success(sysUserService.checkExistsUserByUserIdAndSchoolId(userId, schoolId));
    }

    @GetMapping(value = "/user/checkExistsUserByUserIdAndAreaCode")
    @ApiOperation(value = "根据用户id和角色编码判断用户是否存在")
    CommonResult<Boolean> checkExistsUserByUserIdAndAreaCode(@RequestParam Long userId, @RequestParam Integer areaCode) {
        return success(sysUserService.checkExistsUserByUserIdAndAreaCode(userId, areaCode));
    }

    @GetMapping(value = "/user/getPadUpgradeUrl")
    @ApiOperation(value = "根据PAD当前版本号返回版本升级信息")
    CommonResult<UpgradeDTO> getPadUpgradeUrl(@RequestParam Integer version) {
        return success(sysUserService.getPadUpgradeUrl(version));
    }

    @GetMapping(value = "/user/getResourcePlatform/code")
    @ApiOperation(value = "获取第三方资源平台授权code")
    CommonResult<String> getResourcePlatformCode() throws Exception {
        return success(sysUserService.getResourcePlatformCode());
    }

    @GetMapping("/user/statistics")
    CommonResult<Map<String, List<Map<String, Object>>>> getUserStatistics() {
        return success(sysUserService.getUserStatistics());
    }

    @GetMapping("/user/school/count")
    CommonResult<Integer> getUserCountBySchoolId(@RequestParam Long schoolId, @RequestParam Integer userRoleType) {
        return success(sysUserService.getUserCountBySchoolId(schoolId, userRoleType));
    }

    @GetMapping("/user/area/count")
    CommonResult<Integer> getUserCountByAreaCode(@RequestParam String areaCode, @RequestParam Integer userRoleType) {
        return success(sysUserService.getUserCountByAreaCode(areaCode, userRoleType));
    }

    @GetMapping("/user/schools/nums")
    CommonResult<Map<String, List<Map<String, Object>>>> getTotalUserNumOfSchool(@RequestParam Long schoolId) {
        return success(sysUserService.getTotalUserNumOfSchool(schoolId));
    }

    @GetMapping("/user/area/nums")
    CommonResult<Map<String, List<Map<String, Object>>>> getTotalUserNumOfArea(@RequestParam String areaCode, @RequestParam Long wholeAreaCode) {
        return success(sysUserService.getTotalUserNumOfArea(areaCode, wholeAreaCode));
    }

    @GetMapping("/user/nullAPI")
    CommonResult<String> getNull() {
        return success("TestAPI");
    }

    @GetMapping("/user/findNameById")
    @ApiOperation(value = "查询用户姓名")
    CommonResult<String> findNameById(@RequestParam Long userId) {
        return success(sysUserService.findNameById(userId));
    }

    @PostMapping("/user/findNameByIds")
    @ApiOperation(value = "根据id列表查询用户名称(name)")
    CommonResult<List<CommonDTO>> findNameByIds(@RequestBody Set<Long> userIds) {
        return success(sysUserService.findNameByIds(userIds));
    }

    @PostMapping("/user/getAreaUserIdByParams")
    @ApiOperation(value = "根据查询条件获取区域用户id列表")
    CommonResult<List<Long>> getAreaUserIdByParams(@RequestBody Map<String, Object> params) {
        return success(sysUserService.getAreaUserIdByParams(params));
    }

    @GetMapping("/user/findUserIdListByName")
    @ApiOperation(value = "根据姓名模糊查询所有用户(未删除且已审核的)的userId列表")
    CommonResult<List<Long>> findUserIdListByName(@RequestParam String name, @RequestParam List<String> roleCodes) {
        return success(sysUserService.findUserIdListByName(name, roleCodes));
    }

    @GetMapping(value = "/user/list/query")
    @ApiOperation(value = "根据姓名、角色、区域编码、学校ID 查询学生用户id列表")
    public CommonResult<List<Long>> getUserIdsByQuery(@RequestParam Map<String, Object> params) {
        return success(sysUserService.getUserIdsByQuery(params));
    }
}
