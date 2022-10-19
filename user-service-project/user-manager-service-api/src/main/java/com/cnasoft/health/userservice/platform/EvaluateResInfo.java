package com.cnasoft.health.userservice.platform;

import lombok.Data;

/**
 * 评测结果
 */
@Data
public class EvaluateResInfo {
    /**
     * 量表编号,编号唯一，数据来源于量表导入Excel里面
     */
    private String poms;
    /**
     * 评测结果 A B C
     */
    private String evalres;
}
