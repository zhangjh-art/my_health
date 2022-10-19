package com.cnasoft.health.userservice.excel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.ClazzDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.mapper.ClazzMapper;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.SchoolStaffClazzMapper;
import com.cnasoft.health.userservice.mapper.SchoolStaffMapper;
import com.cnasoft.health.userservice.model.Clazz;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SchoolStaff;
import com.cnasoft.health.userservice.model.SchoolStaffClazz;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.util.RedisUtils;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 导入班级数据
 *
 * @author ganghe
 * @date 2022/5/16 10:05
 **/
@Component("clazzImportServiceImpl")
@Slf4j
public class ClazzImportServiceImpl extends ImportThreadPool implements IImportDataInterface<ClazzDTO> {

    @Value("${user.password.key}")
    private String key;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    private ImportRecordMapper importRecordMapper;

    @Resource
    private SchoolStaffMapper schoolStaffMapper;

    @Resource
    private IClazzService clazzService;

    @Resource
    private ClazzMapper clazzMapper;

    @Resource
    private SchoolStaffClazzMapper staffClazzMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<ClazzDTO> param, boolean intelligent, List<ClazzDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<ClazzDTO> clazzList;
        if (intelligent) {
            clazzList = list;
        } else {
            clazzList = param.getExcelLoadService().load(param.getFile(), ClazzDTO.class);
        }

        if (CollectionUtils.isEmpty(clazzList)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord record = new ImportRecord(param.getImportTypeEnum().getCode());
        record.setFileName(param.getFile().getOriginalFilename());
        record.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        record.setProgress(StudentConstant.PROCESSING);
        record.setTotalNum(clazzList.size());
        importRecordMapper.insert(record);

        //2、分批处理数据
        beanValid(param, record, clazzList);
        return record;
    }

    @Override
    public void beanValid(ImportParam<ClazzDTO> param, ImportRecord record, List<ClazzDTO> list) throws Exception {
        List<ClazzDTO> okList = new ArrayList<>();
        List<ClazzDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<ClazzDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<ClazzDTO> dupList = map.get(false);
        dupList.forEach(e -> {
            if (StringUtils.isEmpty(e.getErrorMsg())) {
                e.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
            } else {
                e.setErrorMsg(e.getErrorMsg() + ";" + StudentErrorCodeConstant.DUPLICATE.getMessage());
            }
            e.setSuccess(false);
        });

        if (!dupList.isEmpty()) {
            errList.addAll(dupList);
        }

        //去掉重复的数据
        List<ClazzDTO> okList2 = map.get(true);
        businessValid(param, okList2, errList, record);
    }

    @Override
    public void businessValid(ImportParam<ClazzDTO> param, List<ClazzDTO> okList, List<ClazzDTO> errList, ImportRecord record) throws Exception {
        List<ClazzDTO> updateDTOList = new ArrayList<>();
        List<ClazzDTO> insertDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertDTOList, updateDTOList, errList, record);
            return;
        }

        // 查询班主任信息
        List<String> mobiles = okList.stream().map(ClazzDTO::getMobile).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(mobiles)) {
            List<ClazzDTO> tempList = new ArrayList<>();
            List<SchoolStaff> schoolStaffIds = schoolStaffMapper.getSchoolStaffIdsByMobiles(mobiles, param.getSchoolId(), key);
            if (CollectionUtils.isNotEmpty(schoolStaffIds)) {
                Map<String, Long> mobileAndIdMap = schoolStaffIds.stream().collect(Collectors.toMap(SchoolStaff::getMobile, SchoolStaff::getId, (key1, key2) -> key2));
                okList.forEach(clazz -> {
                    if (StringUtils.isNotBlank(clazz.getMobile())) {
                        if (mobileAndIdMap.containsKey(clazz.getMobile())) {
                            clazz.setSchoolStaffId(mobileAndIdMap.get(clazz.getMobile()));
                        } else {
                            clazz.setErrorMsg("班主任手机号码不存在");
                            clazz.success(false);
                            tempList.add(clazz);
                        }
                    }
                });
            } else {
                okList.forEach(clazz -> {
                    if (StringUtils.isNotBlank(clazz.getMobile())) {
                        clazz.setErrorMsg("班主任手机号码不存在");
                        clazz.success(false);
                        tempList.add(clazz);
                    }
                });
            }

            if (CollectionUtils.isNotEmpty(tempList)) {
                okList.removeAll(tempList);
                errList.addAll(tempList);
            }
        }

        List<SysDictDTO> dictData = RedisUtils.getDictData("SchoolGrade");
        List<ClazzDTO> tempList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dictData)) {
            okList.forEach(clazz -> {
                clazz.setErrorMsg("年级信息不存在");
                clazz.success(false);
                tempList.add(clazz);
            });
        } else {
            Map<String, String> gradeMap = dictData.stream().collect(Collectors.toMap(SysDictDTO::getDictName, SysDictDTO::getDictValue, (key1, key2) -> key2));
            okList.forEach(clazz -> {
                if (!gradeMap.containsKey(clazz.getGrade())) {
                    clazz.setErrorMsg("年级信息不存在");
                    clazz.success(false);
                    tempList.add(clazz);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(tempList)) {
            okList.removeAll(tempList);
            errList.addAll(tempList);
        }

        // 获取当前学校的年级和班级信息
        Map<String, String> clazzMap = new HashMap<>(16);
        List<com.cnasoft.health.common.dto.ClazzDTO> clazzList = clazzService.listAll(param.getSchoolId());
        if (CollectionUtils.isNotEmpty(clazzList)) {
            clazzList.forEach(clazz -> {
                SysDictDTO dictDTO = RedisUtils.getSingleDictData(clazz.getGrade());
                if (Objects.nonNull(dictDTO)) {
                    clazzMap.put(dictDTO.getDictName() + "###" + clazz.getClazzName(), dictDTO.getDictValue() + "###" + clazz.getId());
                }
            });
        }
        if (clazzMap.size() > 0) {
            okList.forEach(clazz -> {
                // 年级和班级
                String key = clazz.getGrade() + "###" + clazz.getClazzName();
                if (clazzMap.containsKey(key)) {
                    String value = clazzMap.get(key);
                    String[] data = value.split("###");
                    clazz.setId(Long.parseLong(data[1]));
                    clazz.setClazzId(Long.parseLong(data[1]));

                    // 根据school_staff_id和clazz_id查询数据
                    if (Objects.nonNull(clazz.getSchoolStaffId())) {
                        LambdaQueryWrapper<SchoolStaffClazz> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(SchoolStaffClazz::getSchoolStaffId, clazz.getSchoolStaffId());
                        queryWrapper.eq(SchoolStaffClazz::getClazzId, clazz.getId());
                        SchoolStaffClazz staffClazz = staffClazzMapper.selectOne(queryWrapper);
                        if (Objects.nonNull(staffClazz)) {
                            clazz.setSchoolStaffClazzId(staffClazz.getId());
                        }
                    }
                }
            });
        }

        updateDTOList = okList.stream().filter(e -> Objects.nonNull(e.getId())).collect(Collectors.toList());
        insertDTOList = okList.stream().filter(e -> Objects.isNull(e.getId())).collect(Collectors.toList());

        //如果不覆盖，直接提示用户数据重复
        if (!param.getCover()) {
            List<ClazzDTO> tempLists = new ArrayList<>();
            for (ClazzDTO clazzDTO : updateDTOList) {
                clazzDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                tempLists.add(clazzDTO);
            }
            errList.addAll(tempLists);

            //如果不覆盖，就需要把重复数据加入错误数据集中
            updateDTOList.removeAll(tempLists);
        }

        //数据转换
        Map<String, String> gradeMap = dictData.stream().collect(Collectors.toMap(SysDictDTO::getDictName, SysDictDTO::getDictValue, (key1, key2) -> key2));
        if (CollectionUtils.isNotEmpty(updateDTOList)) {
            updateDTOList.forEach(clazz -> {
                if (gradeMap.containsKey(clazz.getGrade())) {
                    clazz.setGrade(gradeMap.get(clazz.getGrade()));
                }
            });
        }
        if (CollectionUtils.isNotEmpty(insertDTOList)) {
            insertDTOList.forEach(clazz -> {
                if (gradeMap.containsKey(clazz.getGrade())) {
                    clazz.setGrade(gradeMap.get(clazz.getGrade()));
                }
            });
        }
        updateData(param, insertDTOList, updateDTOList, errList, record);
    }

    @Override
    public void updateData(ImportParam<ClazzDTO> resultParams, List<ClazzDTO> insertDTOList, List<ClazzDTO> updateDTOList, List<ClazzDTO> errList, ImportRecord record) throws Exception {
        List<ClazzDTO> allData = new ArrayList<>();
        allData.addAll(insertDTOList);
        allData.addAll(updateDTOList);

        //分别处理返回回来的数据
        if (CollectionUtils.isNotEmpty(allData)) {
            // 批量更新班级信息
            List<Object> clazzData = new ArrayList<>();
            allData.forEach(e -> {
                Clazz clazz = e.getClazz(resultParams);
                clazzData.add(clazz);
            });
            UserUtil.saveOrUpdateBatch(clazzData, clazzMapper);

            //转换成Map
            List<Clazz> clazzList = (List<Clazz>) (List) clazzData;
            Map<String, Long> idsMap = new HashMap<>(16);
            clazzList.forEach(clazz -> idsMap.put(clazz.getGrade() + "###" + clazz.getClazzName(), clazz.getId()));
            allData.forEach(e -> {
                String key = e.getGrade() + "###" + e.getClazzName();
                if (idsMap.containsKey(key)) {
                    e.setClazzId(idsMap.get(key));
                }
            });

            // 批量更新班主任与班级绑定关系
            List<Object> headerTeacherData = new ArrayList<>();
            allData.forEach(e -> {
                SchoolStaffClazz staffClazz = e.getStaffClazz(resultParams);
                if (Objects.nonNull(staffClazz.getSchoolStaffId())) {
                    headerTeacherData.add(staffClazz);
                }
            });
            if (CollectionUtils.isNotEmpty(headerTeacherData)) {
                UserUtil.saveOrUpdateBatch(headerTeacherData, staffClazzMapper);
            }
        }

        int successNum = 0;
        if (CollectionUtils.isNotEmpty(allData)) {
            successNum += allData.size();
        }

        record.setSuccessNum(successNum);
        record.setFailNum(errList.size());
        record.setUpdateTime(resultParams.getDateInterface().now());
        record.setProgress(StudentConstant.FINISHED);

        if (CollectionUtils.isNotEmpty(errList)) {
            String downloadUrl = errorWriteToExcel(resultParams.getFileService(), resultParams.getErrorFilePath(), errList);
            //验证未通过的数据生成excel，供下载用
            record.setFailPath(downloadUrl);
        }

        record.setUpdateBy(resultParams.getUpdateBy());
        importRecordMapper.updateById(record);
    }

    @Override
    public void saveBatch(List<ClazzDTO> list, ImportParam<ClazzDTO> importParam) throws Exception {

    }

    @Override
    public String errorWriteToExcel(IFileService<ClazzDTO> fileService, String fileName, List<ClazzDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, ClazzDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public List<SysUser> unique(List<String> keys) {
        return null;
    }

    @Override
    public Map<Boolean, List<ClazzDTO>> duplicate(List<ClazzDTO> sourceList) {
        Map<Boolean, List<ClazzDTO>> resultMap = new HashMap<>(16);
        List<ClazzDTO> okList = new ArrayList<>();
        List<ClazzDTO> errList = new ArrayList<>();

        Map<String, List<ClazzDTO>> map = sourceList.stream().collect(Collectors.groupingBy(item -> item.getGrade() + "###" + item.getClazzName()));
        map.forEach((gradeAndClazz, ClazzDTOList) -> {
            if (ClazzDTOList.size() > 1) {
                okList.add(ClazzDTOList.get(0));
                errList.addAll(ClazzDTOList.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(ClazzDTOList);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }
}
