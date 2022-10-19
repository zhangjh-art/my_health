package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户文件夹表
 *
 * @author ganghe
 */
@TableName(value = "user_folder")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserFolder extends SuperModel<UserFolder> {

    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 父文件夹id
     */
    private Long parentId;

    /**
     * 文件夹名称
     */
    private String name;
}