package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 操作日志
 *
 * @author ganghe
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_operate_log")
public class SysOperateLog {
    @TableId
    private Long id;

    /**
     * 命令ID
     */
    private String commandId;

    /**
     * 命令类型：INSERT/UPDATE/DELETE
     */
    private String commandType;

    /**
     * 命令执行的实体类名称
     */
    private String commandModel;

    /**
     * 命令执行的SQL语句
     */
    private String commandSql;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否逻辑删除0: 否, 1: 是
     */
    @TableLogic
    private Boolean isDeleted;
}
