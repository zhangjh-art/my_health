package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("smart_consult_question")
public class SmartConsultQuestion {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 编号
     */
    private Integer sort;

    /**
     * 类型
     */
    private String type;

    /**
     * 子类型
     */
    private String subType;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

}
