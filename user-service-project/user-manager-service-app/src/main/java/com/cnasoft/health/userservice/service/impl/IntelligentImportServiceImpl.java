package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.io.FileUtil;
import com.cnasoft.health.common.util.poi.PoiUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.enums.ImportTypeEnum;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.dto.AreaStaffDTO;
import com.cnasoft.health.userservice.excel.dto.AreaTeacherDTO;
import com.cnasoft.health.userservice.excel.dto.ClazzDTO;
import com.cnasoft.health.userservice.excel.dto.ParentDTO;
import com.cnasoft.health.userservice.excel.dto.SchoolStaffDTO;
import com.cnasoft.health.userservice.excel.dto.SchoolTeacherDTO;
import com.cnasoft.health.userservice.excel.dto.StudentDTO;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.impl.ExcelLoadServiceImpl;
import com.cnasoft.health.userservice.excel.service.impl.FileServiceImpl;
import com.cnasoft.health.userservice.feign.dto.IntelligentImportVO;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.service.IIntelligentImportService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 智能导入
 *
 * @author ganghe
 * @date 2022/5/14 17:45
 **/
@Slf4j
@Service
public class IntelligentImportServiceImpl implements IIntelligentImportService {

    static final String CHAR = "?";

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource(name = "studentImportServiceImpl")
    private IImportDataInterface<StudentDTO> studentImportService;

    @Resource(name = "parentImportServiceImpl")
    private IImportDataInterface<ParentDTO> parentImportService;

    @Resource(name = "schoolStaffImportServiceImpl")
    private IImportDataInterface<SchoolStaffDTO> schoolStaffImportService;

    @Resource(name = "schoolTeacherImportServiceImpl")
    private IImportDataInterface<SchoolTeacherDTO> schoolTeacherImportService;

    @Resource(name = "areaStaffImportServiceImpl")
    private IImportDataInterface<AreaStaffDTO> areaStaffImport;

    @Resource(name = "areaTeacherImportServiceImpl")
    private IImportDataInterface<AreaTeacherDTO> areaTeacherImport;

    @Resource(name = "clazzImportServiceImpl")
    private IImportDataInterface<ClazzDTO> clazzImportService;

    @Override
    public ImportRecord importData(MultipartFile file, IntelligentImportVO importVO) throws Exception {
        // 将文件上传到天翼云临时文件夹
        CommonResult<List<String>> uploadResult = fileFeignClient.uploadFileToOss(new MultipartFile[] {file}, "false");
        uploadResult.checkError();

        File tempFile = FileUtil.transferToFile(file);
        if (Objects.isNull(tempFile)) {
            throw exception("文件转换出错，请重试");
        }

        String[] header = PoiUtil.readExcelHeader(tempFile, 0);
        if (ArrayUtils.isEmpty(header)) {
            throw exception("读取文件头出错，请重试");
        }

        Field[] fields = null;
        if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.STUDENT)) {
            fields = ReflectUtil.getFields(StudentDTO.class);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.PARENT)) {
            fields = ReflectUtil.getFields(ParentDTO.class);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.SCHOOL_STAFF)) {
            fields = ReflectUtil.getFields(SchoolStaffDTO.class);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.SCHOOL_TEACHER)) {
            fields = ReflectUtil.getFields(SchoolTeacherDTO.class);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.AREA_STAFF)) {
            fields = ReflectUtil.getFields(AreaStaffDTO.class);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.AREA_TEACHER)) {
            fields = ReflectUtil.getFields(AreaTeacherDTO.class);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.CLAZZ)) {
            fields = ReflectUtil.getFields(ClazzDTO.class);
        }

        if (ArrayUtils.isEmpty(fields)) {
            throw exception("不支持的导入类型");
        }

        // 校验该文件是否符合标准模板
        Map<String, String> columnMapping = new HashMap<>(16);
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelProperty.class)) {
                ExcelProperty excel = field.getAnnotation(ExcelProperty.class);
                String value = excel.value()[0];
                if (StringUtils.isNotBlank(value)) {
                    columnMapping.put(field.getName(), value.replace("*", ""));
                }
            }
        }

        boolean matchTemplate = true;
        for (String title : header) {
            boolean match = false;
            for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
                if (title.startsWith(entry.getValue())) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                matchTemplate = false;
                break;
            }
        }

        // 删除临时文件
        tempFile.deleteOnExit();

        ImportRecord importRecord;
        if (matchTemplate) {
            File downloadFile = downloadFile(uploadResult.getData().get(0));
            MultipartFile multipartFile = FileUtil.getMultipartFile(downloadFile);
            importRecord = importDataPb(importVO, multipartFile, null);
            if (Objects.nonNull(importRecord)) {
                importRecord.setIsTemplate(true);
            }

            // 删除临时文件
            downloadFile.deleteOnExit();
        } else {
            importRecord = new ImportRecord();
            importRecord.setTempFilePath(uploadResult.getData().get(0));
            importRecord.setHeader(header);
            importRecord.setColumnMapping(columnMapping);
            importRecord.setIsTemplate(false);
        }
        return importRecord;
    }

    @Override
    public ImportRecord importDataWithHeaders(String filePath, IntelligentImportVO importVO) throws Exception {
        Class<?> clazz = null;
        if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.STUDENT)) {
            clazz = StudentDTO.class;
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.PARENT)) {
            clazz = ParentDTO.class;
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.SCHOOL_STAFF)) {
            clazz = SchoolStaffDTO.class;
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.SCHOOL_TEACHER)) {
            clazz = SchoolTeacherDTO.class;
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.AREA_STAFF)) {
            clazz = AreaStaffDTO.class;
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.AREA_TEACHER)) {
            clazz = AreaTeacherDTO.class;
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.CLAZZ)) {
            clazz = ClazzDTO.class;
        }

        if (Objects.isNull(clazz)) {
            throw exception("不支持的导入类型");
        }

        Map<String, String> columnMap = JsonUtils.readValue(importVO.getColumnMapping(), new TypeReference<Map<String, String>>() {
        });

        File tempFile = downloadFile(filePath);
        Map<String, Object> dataMap = PoiUtil.readExcel(tempFile, 0, false);

        List<Object> objectList = new ArrayList<>();
        List<Map<String, String>> dataList = (List<Map<String, String>>)dataMap.get("data");
        for (Map<String, String> map : dataList) {
            Object object = ReflectUtil.newInstance(clazz);

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String fieldName = StringUtils.EMPTY;
                for (Map.Entry<String, String> columnEntry : columnMap.entrySet()) {
                    if (entry.getKey().equals(columnEntry.getValue())) {
                        fieldName = columnEntry.getKey();
                        break;
                    }
                }
                if (StringUtils.isNotBlank(fieldName)) {
                    if (StringUtils.isBlank(entry.getValue())) {
                        ReflectUtil.setFieldValue(object, fieldName, null);
                    } else {
                        ReflectUtil.setFieldValue(object, fieldName, entry.getValue());
                    }
                }
            }

            objectList.add(object);
        }

        MultipartFile file = FileUtil.getMultipartFile(tempFile);
        ImportRecord importRecord = importDataPb(importVO, file, objectList);

        // 删除临时文件
        tempFile.deleteOnExit();
        return importRecord;
    }

    private ImportParam.Builder<Object> getImportParamBuilder(MultipartFile file, IntelligentImportVO importVO) {
        ImportTypeEnum importTypeEnum = ImportTypeEnum.getImportType(importVO.getImportType());

        return new ImportParam.Builder<>(importVO.getCover(), importTypeEnum).setFileService(new FileServiceImpl<>()).setExcelLoadService(new ExcelLoadServiceImpl<>())
            .setTemplatePath("").setErrorFilePath(String.format("%s_error.xls", file.getOriginalFilename())).setFile(file).setCover(importVO.getCover()).setDateInterface(Date::new)
            .setAreaCode(importVO.getAreaCode()).setUserId(importVO.getUserId()).setRoleCode(importVO.getRoleCode()).setCreateBy(importVO.getUserId())
            .setUpdateBy(importVO.getUserId()).setSchoolId(importVO.getSchoolId());
    }

    /**
     * 下载文件到临时目录
     *
     * @param filePath
     * @return
     */
    private File downloadFile(String filePath) {
        String fileName = getFileName(filePath);
        String tmpDir = System.getProperty("java.io.tmpdir");
        File downloadFile = new File(tmpDir + "/" + fileName);
        try {
            HttpUtil.downloadFile(filePath, downloadFile, 60000);
        } catch (Exception e) {
            throw exception("下载文件失败");
        }

        return downloadFile;
    }

    /**
     * 导入数据公共方法
     *
     * @param importVO      参数
     * @param multipartFile 文件
     * @return 导入记录
     * @throws Exception 异常
     */
    private ImportRecord importDataPb(IntelligentImportVO importVO, MultipartFile multipartFile, List<Object> objectList) throws Exception {
        ImportRecord importRecord = null;
        if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.STUDENT)) {
            // 导入学生数据
            importRecord = importStudent(multipartFile, importVO, objectList);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.PARENT)) {
            // 导入家长数据
            importRecord = importParent(multipartFile, importVO, objectList);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.SCHOOL_STAFF)) {
            // 导入校教职工数据
            importRecord = importSchoolStaff(multipartFile, importVO, objectList);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.SCHOOL_TEACHER)) {
            // 导入校心理老师数据
            importRecord = importSchoolTeacher(multipartFile, importVO, objectList);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.AREA_STAFF)) {
            // 导入区域职员数据
            importRecord = importAreaStaff(multipartFile, importVO, objectList);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.AREA_TEACHER)) {
            // 导入区域心理教研员数据
            importRecord = importAreaTeacher(multipartFile, importVO, objectList);
        } else if (Objects.equals(ImportTypeEnum.getImportType(importVO.getImportType()), ImportTypeEnum.CLAZZ)) {
            // 导入班级数据
            importRecord = importClazz(multipartFile, importVO, objectList);
        }

        return importRecord;
    }

    private ImportRecord importStudent(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<StudentDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<StudentDTO> list = (List)objectList;
            return studentImportService.importExcel(importParam.build(), true, list);
        } else {
            return studentImportService.importExcel(importParam.build(), false, null);
        }
    }

    private ImportRecord importParent(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<ParentDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<ParentDTO> list = (List)objectList;
            return parentImportService.importExcel(importParam.build(), true, list);
        } else {
            return parentImportService.importExcel(importParam.build(), false, null);
        }
    }

    private ImportRecord importSchoolStaff(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<SchoolStaffDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<SchoolStaffDTO> list = (List)objectList;
            return schoolStaffImportService.importExcel(importParam.build(), true, list);
        } else {
            return schoolStaffImportService.importExcel(importParam.build(), false, null);
        }
    }

    private ImportRecord importSchoolTeacher(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<SchoolTeacherDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<SchoolTeacherDTO> list = (List)objectList;
            return schoolTeacherImportService.importExcel(importParam.build(), true, list);
        } else {
            return schoolTeacherImportService.importExcel(importParam.build(), false, null);
        }
    }

    private ImportRecord importAreaStaff(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<AreaStaffDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<AreaStaffDTO> list = (List)objectList;
            return areaStaffImport.importExcel(importParam.build(), true, list);
        } else {
            return areaStaffImport.importExcel(importParam.build(), false, null);
        }
    }

    private ImportRecord importAreaTeacher(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<AreaTeacherDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<AreaTeacherDTO> list = (List)objectList;
            return areaTeacherImport.importExcel(importParam.build(), true, list);
        } else {
            return areaTeacherImport.importExcel(importParam.build(), false, null);
        }
    }

    private ImportRecord importClazz(MultipartFile file, IntelligentImportVO importVO, List<Object> objectList) throws Exception {
        ImportParam.Builder<ClazzDTO> importParam = (ImportParam.Builder)getImportParamBuilder(file, importVO);

        if (CollectionUtils.isNotEmpty(objectList)) {
            List<ClazzDTO> list = (List)objectList;
            return clazzImportService.importExcel(importParam.build(), true, list);
        } else {
            return clazzImportService.importExcel(importParam.build(), false, null);
        }
    }

    private String getFileName(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.contains(CHAR)) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        return fileName;
    }
}
