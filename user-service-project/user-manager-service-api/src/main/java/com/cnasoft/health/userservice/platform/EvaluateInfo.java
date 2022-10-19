package com.cnasoft.health.userservice.platform;

import lombok.Data;

/**
 * 评测结果集
 */
@Data
public class EvaluateInfo {
    /**
     * 量表编号,编号唯一，数据来源于量表导入Excel里面
     */
    private String poms;
    /**
     * 评测等级 如果存在数组，以|分割。如 A|B|C|D|E
     */
    private String levels;
}
