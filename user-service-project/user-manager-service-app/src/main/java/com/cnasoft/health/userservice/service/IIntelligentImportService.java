package com.cnasoft.health.userservice.service;

import com.cnasoft.health.userservice.feign.dto.IntelligentImportVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * 智能导入
 *
 * @author ganghe
 * @date 2022/5/14 17:42
 **/
public interface IIntelligentImportService {

    /**
     * 导入数据
     *
     * @param file     文件
     * @param importVO 导入参数
     * @return 导入记录
     * @throws Exception 异常
     */
    ImportRecord importData(MultipartFile file, IntelligentImportVO importVO) throws Exception;

    /**
     * 导入数据
     *
     * @param filePath 文件路径
     * @param importVO 导入参数
     * @return 导入记录
     * @throws Exception 异常
     */
    ImportRecord importDataWithHeaders(String filePath, IntelligentImportVO importVO) throws Exception;
}
