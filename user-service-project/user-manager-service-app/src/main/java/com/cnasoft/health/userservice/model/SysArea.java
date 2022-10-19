package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lgf on
 * @date 2022/3/23.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_area")
public class SysArea extends SuperModel<SysArea> {
    @TableId
    private Long id;

    /**
     * 类型：0=省,1=市,2=区
     */
    private Integer type;

    /**
     * 区域编码
     */
    private Integer code;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 启用状态：false禁用,true启用
     */
    private Boolean enabled;

    /**
     * 审核状态：0未审核,1通过,2拒绝
     */
    private Integer approveStatus;
}
