package com.cnasoft.health.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SysUserDTO implements UserDetails, Serializable {
    /**
     * 用户id (Why String? 请参考: <<阿里巴巴java开发手册嵩山版>> page 29 第6条.
     * 对于需要使用超大整数的场景，服务端一律使用String字符串类型返回，禁止使用Long类型。)
     */
    @ApiModelProperty(value = "用户ID-字符串")
    private String idStr;

    @ApiModelProperty(value = "用户ID")
    private Long id;

    @ApiModelProperty(value = "用户名-登录账号")
    private String username;

    @ApiModelProperty(value = "学生短id")
    private String shortId;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "性别")
    private Integer sex;

    @ApiModelProperty(value = "电子邮箱")
    private String email;

    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;

    @ApiModelProperty(value = "审核状态：0未审核,1通过,2拒绝")
    private Integer approveStatus;

    @ApiModelProperty(value = "openId")
    private String openId;

    @ApiModelProperty(value = "区域编码")
    private Integer areaCode;

    @ApiModelProperty(value = "区域名称")
    private String areaName;

    @ApiModelProperty(value = "学校id")
    private Long schoolId;

    @ApiModelProperty(value = "学校名称")
    private String schoolName;

    @ApiModelProperty(value = "学校列表")
    private List<SchoolDTO> schools;

    @ApiModelProperty(value = "内置角色编码")
    private String presetRoleCode;

    @ApiModelProperty(value = "自定义角色编码列表")
    private Set<String> customRoleCodes;

    @ApiModelProperty(value = "权限列表")
    private Set<SysAuthoritySimpleDTO> permissions;

    @ApiModelProperty(value = "权限编码列表")
    private Set<String> permissionCodes;

    @ApiModelProperty(value = "是否已激活,0: 否, 1: 是")
    private Boolean isActive;

    @ApiModelProperty(value = "是否已确认,0: 否, 1: 是")
    private Boolean confirmed;

    @ApiModelProperty(value = "是否已删除,0: 否, 1: 是")
    private Boolean isDeleted;

    @ApiModelProperty(value = "账号是否已脱敏")
    private Boolean desensitized;

    @ApiModelProperty(value = "是否首次登录")
    private Boolean firstLogin;

    @ApiModelProperty(value = "审核时的备注信息")
    private String remark;

    @ApiModelProperty(value = "是否承接任务")
    private Boolean isAcceptTask;

    @ApiModelProperty(value = "姓名修改次数")
    private Integer nameChange;

    /***
     * 权限重写
     */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new HashSet<>();

        if (StringUtils.isNotBlank(this.getPresetRoleCode())) {
            collection.add(new SimpleGrantedAuthority(this.getPresetRoleCode()));
        }

        if (!CollectionUtils.isEmpty(this.getCustomRoleCodes())) {
            this.getCustomRoleCodes().parallelStream().forEach(role -> collection.add(new SimpleGrantedAuthority(role)));
        }
        return collection;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getEnabled();
    }

    @JsonIgnore
    public boolean isSchool() {
        return Objects.nonNull(schoolId) && schoolId > 0;
    }
}
