package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 学生导入记录表
 */
@Data
@TableName("import_record")
@EqualsAndHashCode(callSuper = false)
public class ImportRecord extends SuperModel<ImportRecord> {

    public ImportRecord() {

    }

    public ImportRecord(Integer importType) {
        this.importType = importType;
    }

    @TableId
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 绝对保存地址
     */
    private String savePath;

    /**
     * 错误文件地址
     */
    private String failPath;

    /**
     * 总条数
     */
    private Integer totalNum;

    /**
     * 成功条数
     */
    private Integer successNum;

    /**
     * 失败条数
     */
    private Integer failNum;

    /**
     * 1：系统模板；2：自定义模板
     */
    private String templateType;

    /**
     * 1：学生；2：家长；3：校教职工；4：校心理教研员；5：区域职员；6：区域心理教研员
     */
    private Integer importType;

    /**
     * 处理进度，1：进行中；2：完毕。
     */
    private Integer progress;

    /**
     * 天翼云临时文件全路径
     */
    @TableField(exist = false)
    private Boolean isTemplate;

    /**
     * 天翼云临时文件全路径
     */
    @TableField(exist = false)
    private String tempFilePath;

    /**
     * Excel文件标题行
     */
    @TableField(exist = false)
    private String[] header;

    /**
     * Excel文件列标题与实体属性映射
     */
    @TableField(exist = false)
    Map<String, String> columnMapping;
}
