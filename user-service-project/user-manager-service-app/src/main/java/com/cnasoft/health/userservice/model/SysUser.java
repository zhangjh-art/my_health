package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author ganghe
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser extends SuperModel<SysUser> {
    @TableId
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 短id
     */
    private String shortId;

    /**
     * 姓名
     */
    private String name;
    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String headImgUrl;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 性别: 1: 男，2: 女
     */
    private Integer sex;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 开放给第三方服务的openId
     */
    private String openId;

    /**
     * 区域code
     */
    private Integer areaCode;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 启用状态: 0: 否, 1: 是
     */
    private Boolean enabled;

    /**
     * 审核状态 0 待审核 1 审核通过 2 已被拒绝 默认 1
     */
    private Integer approveStatus;

    /**
     * 首次登陆：true：是，false：不是
     */
    private Boolean firstLogin;

    /**
     * 姓名修改次数
     */
    private Integer nameChange;

    /**
     * 角色编码
     */
    private String roleCode;

    @TableField(exist = false)
    private List<String> roleCodes;

    @TableField(exist = false)
    private String roleId;

    @TableField(exist = false)
    private String key;

    @TableField(exist = false)
    private Long clazzId;

    @TableField(exist = false)
    private String remark;

    @TableField(exist = false)
    private String schoolName;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public SysUser(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
