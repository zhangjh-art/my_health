package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户文件表
 *
 * @author ganghe
 */
@TableName(value = "user_file")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserFile extends SuperModel<UserFile> {

    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 文件夹id
     */
    private Long folderId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小，单位：字节(B)
     */
    private Long fileSize;

    /**
     * 文件的MD5值
     */
    private String fileMd5;

    /**
     * 文件key,可生成访问路径，有效期7天
     */
    private String fileKey;
}