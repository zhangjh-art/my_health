package com.cnasoft.health.userservice.excel.service;

import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SysUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Component
public interface IImportDataInterface<T> {

    /**
     * 导入文件
     *
     * @param param
     * @param intelligent
     * @return
     */
    ImportRecord importExcel(ImportParam<T> param, boolean intelligent, List<T> list) throws Exception;

    /**
     * Bean的验证
     *
     * @param param
     * @param record
     * @param list
     * @throws InterruptedException
     */
    void beanValid(ImportParam<T> param, ImportRecord record, List<T> list) throws Exception;

    /**
     * 业务验证
     *
     * @param param
     * @param okList
     * @param errList
     * @param record
     * @return
     */
    void businessValid(ImportParam<T> param, List<T> okList, List<T> errList, ImportRecord record) throws Exception;

    /**
     * 处理数据入库
     *
     * @param resultParams
     * @param insertStaffDTOS
     * @param updateStaffDTOS
     * @param errList
     * @param record
     * @return
     */
    void updateData(ImportParam<T> resultParams, List<T> insertStaffDTOS, List<T> updateStaffDTOS, List<T> errList, ImportRecord record) throws Exception;

    /**
     * 批量保存
     *
     * @param list        要保存的数据
     * @param importParam 导入参数
     */
    void saveBatch(List<T> list, ImportParam<T> importParam) throws Exception;

    /**
     * 生成错误文件
     *
     * @param fileService 文件服务
     * @param fileName    文件名称
     * @param errList     导入内容
     * @return 文件路径
     */
    String errorWriteToExcel(IFileService<T> fileService, String fileName, List<T> errList);

    /**
     * 从数据库中找出重复的
     *
     * @param keys
     * @return
     */
    List<SysUser> unique(List<String> keys) throws Exception;

    /**
     * 对文件里面的数据判断重复
     *
     * @return
     */
    Map<Boolean, List<T>> duplicate(List<T> sourceList);
}
