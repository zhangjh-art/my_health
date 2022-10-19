package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试管理员密码管理
 *
 * @author ganghe
 * @date 2022/7/15 10:14
 **/
@Data
@TableName("test_manager_password")
@EqualsAndHashCode(callSuper = false)
public class TestManagerPassword extends SuperModel<TestManagerPassword> {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 测试管理员用户id
     */
    private Long userId;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 密码
     */
    private String password;

    /**
     * 应用场景：1(锁屏)，2(返回首页)，3(退出系统)
     */
    private Integer applicationScene;

    @TableField(exist = false)
    private String key;
}
