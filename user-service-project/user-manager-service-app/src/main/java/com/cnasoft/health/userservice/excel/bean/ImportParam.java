package com.cnasoft.health.userservice.excel.bean;

import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.service.IDateInterface;
import com.cnasoft.health.userservice.excel.service.IExcelLoadService;
import com.cnasoft.health.userservice.excel.service.IFileService;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
public class ImportParam<T> {
    /**
     * 错误文件路径
     */
    private final String errorFilePath;

    /**
     * 错误文件下载路径
     */
    private String errorFileDownLoadPath;

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
     * 是否覆盖
     */
    private final Boolean cover;

    /**
     * 模板文件路径
     */
    private final String templatePath;

    /**
     * 导入接入：true：全部成功，false:失败或部分失败
     */
    private final Boolean importResult;

    /**
     * Excel错误文件存储接口
     */
    private final IFileService<T> fileService;

    /**
     * Excel文件加载接口
     */
    private final IExcelLoadService<T> excelLoadService;

    /**
     * 导入数据类型
     */
    private final ImportTypeEnum importTypeEnum;

    /**
     * 上传文件
     */
    private final MultipartFile file;

    /**
     * 时间处理
     */
    private IDateInterface dateInterface;

    /**
     * 创建人
     */
    private final Long createBy;

    /**
     * 更新人
     */
    private final Long updateBy;

    /**
     * 登录人区域ID
     */
    private final Integer areaCode;

    /**
     * 登录人学校
     */
    private final Long schoolId;

    /**
     * 当前登录人
     */
    private final Long userId;

    /**
     * 当前登录人角色
     */
    private final String roleCode;

    private Map<String, Long> map;

    private ImportParam(Builder<T> builder) {
        this.cover = builder.cover;
        this.templatePath = builder.templatePath;
        this.areaCode = builder.areaCode;
        this.schoolId = builder.schoolId;
        this.importResult = builder.importResult;
        this.fileService = builder.fileService;
        this.importTypeEnum = builder.importTypeEnum;
        this.file = builder.file;
        this.dateInterface = builder.dateInterface;
        this.createBy = builder.createBy;
        this.updateBy = builder.updateBy;
        this.userId = builder.userId;
        this.roleCode = builder.roleCode;
        this.errorFilePath = builder.errorFilePath;
        this.errorFileDownLoadPath = builder.errorFileDownLoadPath;
        this.successNum = builder.successNum;
        this.failNum = builder.failNum;
        this.excelLoadService = builder.excelLoadService;
        this.map = builder.map;
    }

    public static class Builder<T> {
        private String errorFilePath;
        private String errorFileDownLoadPath;
        private Integer successNum;
        private Integer failNum;
        private Boolean cover;
        private IDateInterface dateInterface;
        private String templatePath;
        private Integer areaCode;
        private Long schoolId;
        private Boolean importResult;
        private Long userId;
        private String roleCode;
        private IFileService<T> fileService;
        private IExcelLoadService<T> excelLoadService;
        private ImportTypeEnum importTypeEnum;
        private MultipartFile file;
        private Long createBy;
        private Long updateBy;
        private Map<String, Long> map;

        public Builder(Boolean cover, ImportTypeEnum importTypeEnum) {
            this.cover = cover;
            this.importTypeEnum = importTypeEnum;
        }

        public Builder<T> setFile(MultipartFile file) {
            this.file = file;
            return this;
        }

        public Builder<T> setTemplatePath(String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        public Builder<T> setExcelLoadService(IExcelLoadService<T> excelLoadService) {
            this.excelLoadService = excelLoadService;
            return this;
        }

        public Builder<T> setFileService(IFileService<T> fileService) {
            this.fileService = fileService;
            return this;
        }

        public Builder<T> setDateInterface(IDateInterface dateInterface) {
            this.dateInterface = dateInterface;
            return this;
        }

        public Builder<T> setCover(Boolean cover) {
            this.cover = cover;
            return this;
        }

        public Builder<T> setCreateBy(Long createBy) {
            this.createBy = createBy;
            return this;
        }

        public Builder<T> setUpdateBy(Long updateBy) {
            this.updateBy = updateBy;
            return this;
        }

        public Builder<T> setAreaCode(Integer areaCode) {
            this.areaCode = areaCode;
            return this;
        }

        public Builder<T> setSchoolId(Long schoolId) {
            this.schoolId = schoolId;
            return this;
        }

        public Builder<T> setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder<T> setRoleCode(String roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        public Builder<T> setErrorFilePath(String errorFilePath) {
            this.errorFilePath = errorFilePath;
            return this;
        }

        public Builder<T> setSuccessNum(Integer successNum) {
            this.successNum = successNum;
            return this;
        }

        public Builder<T> setFailNum(Integer failNum) {
            this.failNum = failNum;
            return this;
        }

        public Builder<T> setMap(Map<String, Long> map) {
            this.map = map;
            return this;
        }

        public ImportParam<T> build() {
            return new ImportParam<T>(this);
        }
    }
}
