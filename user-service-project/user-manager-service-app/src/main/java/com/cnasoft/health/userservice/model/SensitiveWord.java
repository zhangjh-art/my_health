package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:41
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sensitive_word")
public class SensitiveWord extends SuperModel<SensitiveWord> {
    @TableId
    private Long id;

    /**
     * 敏感词
     */
    private String text;

    /**
     * 创建人姓名
     */
    @TableField(exist = false)
    private String createByName;
}
