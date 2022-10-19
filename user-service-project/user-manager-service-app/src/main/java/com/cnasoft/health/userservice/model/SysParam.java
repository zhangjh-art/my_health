package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lqz
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_param")
public class SysParam extends SuperModel<SysParam> {
    /**
     * 参数表id
     */
    @TableId
    private Long id;

    /**
     * 参数编码键值:根据该键值查询数据
     */
    private String paramKey;

    /**
     * 参数存储的值
     */
    private String paramValue;
}
