package com.cnasoft.health.userservice.excel.dto;

import lombok.Data;

/**
 * @author ganghe
 * @date 2022/5/14 0:03
 **/
@Data
public class ExcelHeaderDTO {

    /**
     * 工作表序号
     */
    private Integer sheetIndex;

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 标题行
     */
    private String[] header;
}
