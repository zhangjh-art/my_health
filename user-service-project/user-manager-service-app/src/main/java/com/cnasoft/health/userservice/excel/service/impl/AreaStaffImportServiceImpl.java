package com.cnasoft.health.userservice.excel.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.enums.AreaStaffType;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.StudentConstant;
import com.cnasoft.health.userservice.constant.StudentErrorCodeConstant;
import com.cnasoft.health.userservice.excel.bean.ImportParam;
import com.cnasoft.health.userservice.excel.bean.ImportThreadPool;
import com.cnasoft.health.userservice.excel.dto.AreaStaffDTO;
import com.cnasoft.health.userservice.excel.service.IFileService;
import com.cnasoft.health.userservice.excel.service.IImportDataInterface;
import com.cnasoft.health.userservice.excel.service.INeedEncryptService;
import com.cnasoft.health.userservice.mapper.AreaStaffMapper;
import com.cnasoft.health.userservice.mapper.ImportRecordMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.AreaStaff;
import com.cnasoft.health.userservice.model.ImportRecord;
import com.cnasoft.health.userservice.model.SysUser;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Component("areaStaffImportServiceImpl")
@Slf4j
public class AreaStaffImportServiceImpl extends ImportThreadPool implements IImportDataInterface<AreaStaffDTO> {

    @Value("${user.password.key}")
    private String key;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    private ImportRecordMapper importRecordMapper;

    @Resource
    private AreaStaffMapper areaStaffMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private INeedEncryptService<AreaStaffDTO> needEncryptService;

    /**
     * 第一步
     *
     * @param param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImportRecord importExcel(ImportParam<AreaStaffDTO> param, boolean intelligent, List<AreaStaffDTO> list) throws Exception {
        if (null == param.getFile()) {
            throw exception(StudentErrorCodeConstant.NOT_UPLOAD_FILE);
        }

        List<AreaStaffDTO> areaStaffData;
        if (intelligent) {
            areaStaffData = list;
        } else {
            areaStaffData = param.getExcelLoadService().load(param.getFile(), AreaStaffDTO.class);
        }

        if (CollectionUtils.isEmpty(areaStaffData)) {
            throw exception(StudentErrorCodeConstant.PARSE_EXCEL_FAILED);
        }

        //1、保存导入记录
        ImportRecord record = new ImportRecord(param.getImportTypeEnum().getCode());
        record.setFileName(param.getFile().getOriginalFilename());
        record.setSavePath(param.getFile().getOriginalFilename());
        // 处理中
        record.setProgress(StudentConstant.PROCESSING);
        record.setTotalNum(areaStaffData.size());
        importRecordMapper.insert(record);

        //2、分批处理数据
        beanValid(param, record, areaStaffData);
        return record;
    }

    /**
     * 批量保存或更新区域职员信息
     *
     * @param list
     */
    @Override
    public void saveBatch(List<AreaStaffDTO> list, ImportParam<AreaStaffDTO> importParam) throws Exception {
        List<Object> data = new ArrayList<>();
        list.forEach(e -> data.add(e.getSelf(importParam)));

        //批量添加或更新数据
        UserUtil.saveOrUpdateBatch(data, areaStaffMapper);
    }

    @Override
    public String errorWriteToExcel(IFileService<AreaStaffDTO> fileService, String fileName, List<AreaStaffDTO> errList) {
        if (null != fileService) {
            return fileService.saveFile(fileFeignClient, fileName, errList, AreaStaffDTO.ExcelHead.class);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private List<AreaStaff> getAreaStaff(ImportParam<AreaStaffDTO> param, List<AreaStaffDTO> okList, AreaStaffType staffType) throws InterruptedException {
        List<AreaStaff> areaStaffs = new ArrayList<>();
        String roleCode;
        if (staffType.getCode() == 1) {
            roleCode = RoleEnum.region_staff.getValue();
        } else {
            roleCode = RoleEnum.region_leader.getValue();
        }

        List<String> mobiles = okList.stream().filter(r -> r.getType().equals(staffType.getName())).map(AreaStaffDTO::getMobile).collect(Collectors.toList());
        if (CollUtil.isEmpty(mobiles)) {
            return areaStaffs;
        }

        // 数据行数超过100,则分批次处理
        int count = mobiles.size();
        int batchCount = Constant.BATCH_COUNT;
        if (count > batchCount) {
            int times = (count + batchCount - 1) / batchCount;

            for (int i = 0; i < times; i++) {
                if (i == times - 1) {
                    areaStaffs.addAll(areaStaffMapper.selectByAreaStaff(param.getAreaCode(), roleCode, mobiles.subList(i * batchCount, count), key));
                } else {
                    areaStaffs.addAll(
                            areaStaffMapper.selectByAreaStaff(param.getAreaCode(), roleCode, mobiles.subList(i * batchCount, (i + 1) * batchCount), key));
                }
            }
        } else {
            areaStaffs.addAll(areaStaffMapper.selectByAreaStaff(param.getAreaCode(), roleCode, mobiles, key));
        }
        return areaStaffs;
    }

    /**
     * 验证数据库里面是否有重复数据
     *
     * @param param   格式有误的数据
     * @param okList  格式验证通过的数据
     * @param errList 格式有误的数据
     * @param record  格式有误的数据
     */
    @Override
    public void businessValid(ImportParam<AreaStaffDTO> param, List<AreaStaffDTO> okList, List<AreaStaffDTO> errList, ImportRecord record) throws Exception {
        List<AreaStaffDTO> updateDTOList = new ArrayList<>();
        List<AreaStaffDTO> insertDTOList = new ArrayList<>();

        if (CollectionUtils.isEmpty(okList)) {
            updateData(param, insertDTOList, updateDTOList, errList, record);
            return;
        }

        //如果不能覆盖，就得判断数据库中是否有重复数据
        List<AreaStaff> dupAreaStaffs = new ArrayList<>();
        //查询普通职员数据
        dupAreaStaffs.addAll(getAreaStaff(param, okList, AreaStaffType.NORMAL_STAFF));
        //查询领导数据
        dupAreaStaffs.addAll(getAreaStaff(param, okList, AreaStaffType.LEADER));

        //数据库里面重复的数据
        if (CollectionUtils.isNotEmpty(dupAreaStaffs)) {

            Set<String> dupKeys = dupAreaStaffs.stream().map(AreaStaff::getUsername).collect(Collectors.toSet());
            updateDTOList = okList.stream().filter(e -> dupKeys.contains(e.getMobile())).collect(Collectors.toList());
            insertDTOList = okList.stream().filter(e -> !dupKeys.contains(e.getMobile())).collect(Collectors.toList());

            //如果不覆盖，直接提示用户数据重复
            if (Boolean.FALSE.equals(param.getCover())) {
                for (AreaStaffDTO staffDTO : updateDTOList) {
                    staffDTO.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
                    errList.add(staffDTO);
                }
                //如果不覆盖，就需要把重复数据加入错误数据集中
                updateDTOList.clear();
            } else {
                //转为Map
                Map<String, Long> usernameAnduserIdMap = dupAreaStaffs.stream().collect(Collectors.toMap(AreaStaff::getUsername, AreaStaff::getUserId, (key1, key2) -> key2));
                Map<Long, Long> userIdAndStaffIdMap = dupAreaStaffs.stream().collect(Collectors.toMap(AreaStaff::getUserId, AreaStaff::getId, (key1, key2) -> key2));

                //查询出已经存在的Id
                updateDTOList.forEach(e -> {
                    Long userId = usernameAnduserIdMap.get(e.getMobile());
                    if (Objects.nonNull(userId)) {
                        e.setUserId(userId);
                        Long staffId = userIdAndStaffIdMap.get(userId);
                        if (Objects.nonNull(staffId)) {
                            e.setId(staffId);
                        }
                    }
                });
            }
        } else {
            insertDTOList = new ArrayList<>(okList);
        }

        updateData(param, insertDTOList, updateDTOList, errList, record);
    }

    /**
     * 处理数据，入库、更新数据库
     *
     * @param resultParams
     */
    @Override
    public void updateData(ImportParam<AreaStaffDTO> resultParams, List<AreaStaffDTO> insertDTOList, List<AreaStaffDTO> updateDTOList, List<AreaStaffDTO> errList,
        ImportRecord record) throws Exception {
        //分别处理返回回来的数据
        if (CollectionUtils.isNotEmpty(updateDTOList)) {
            List<Object> userData = new ArrayList<>();
            updateDTOList.forEach(e -> {
                userData.add(e.getSysUser(resultParams));
            });
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            // 批量更新区域职工
            saveBatch(updateDTOList, resultParams);
        }

        if (CollectionUtils.isNotEmpty(insertDTOList)) {
            List<Object> userData = new ArrayList<>();
            insertDTOList.forEach(e -> {
                needEncryptService.encrypt(e.password());
                //先存储SysUser,然后获取userId,在存储角色和扩展信息
                userData.add(e.getSysUser(resultParams));
            });
            // 批量保存用户
            UserUtil.saveOrUpdateBatch(userData, sysUserMapper);

            //转换成Map
            List<SysUser> userList = (List)userData;
            Map<String, Long> idsMap = userList.stream().collect(Collectors.toMap(SysUser::getMobile, SysUser::getId, (key1, key2) -> key2));
            //遍历,把保存后的用户userId取过来做关联
            insertDTOList.forEach(e -> {
                Long userId = idsMap.get(e.getMobile());
                if (Objects.nonNull(userId)) {
                    e.setUserId(userId);
                }
            });

            // 批量保存区域职工
            saveBatch(insertDTOList, resultParams);
        }

        int successNum = 0;
        if (CollectionUtils.isNotEmpty(insertDTOList)) {
            successNum += insertDTOList.size();
        }
        if (CollectionUtils.isNotEmpty(updateDTOList)) {
            successNum += updateDTOList.size();
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

    /**
     * 验证数据格式
     *
     * @param param
     * @param record
     * @param list
     */
    @Override
    public void beanValid(ImportParam<AreaStaffDTO> param, ImportRecord record, List<AreaStaffDTO> list) throws Exception {
        List<AreaStaffDTO> okList = new ArrayList<>();
        List<AreaStaffDTO> errList = new ArrayList<>();
        list.forEach(e -> {
            e.validate();
            if (e.success()) {
                okList.add(e);
            } else {
                errList.add(e);
            }
        });

        //验证okList中是否有重复数据
        Map<Boolean, List<AreaStaffDTO>> map = duplicate(okList);
        //把重复的数据加入到错误数据中
        List<AreaStaffDTO> dupList = map.get(false);
        dupList.forEach(e -> {
            if (StringUtils.isEmpty(e.getErrorMsg())) {
                e.setErrorMsg(StudentErrorCodeConstant.DUPLICATE.getMessage());
            } else {
                e.setErrorMsg(e.getErrorMsg() + ";" + StudentErrorCodeConstant.DUPLICATE.getMessage());
            }
        });

        if (!dupList.isEmpty()) {
            errList.addAll(dupList);
        }

        //去掉重复的数据
        List<AreaStaffDTO> okList2 = map.get(true);
        businessValid(param, okList2, errList, record);
    }

    /**
     * 查询结果：返回已经存在的KEYS
     *
     * @param keys
     */
    @Override
    public List<SysUser> unique(List<String> keys) {
        return Collections.emptyList();
    }

    /**
     * 验证导入数据中，是否有重复数据
     *
     * @param sourceList
     * @return
     */
    @Override
    public Map<Boolean, List<AreaStaffDTO>> duplicate(List<AreaStaffDTO> sourceList) {
        Map<Boolean, List<AreaStaffDTO>> resultMap = new HashMap<>(16);
        List<AreaStaffDTO> okList = new ArrayList<>();
        List<AreaStaffDTO> errList = new ArrayList<>();

        Map<String, List<AreaStaffDTO>> map = sourceList.stream().collect(Collectors.groupingBy(AreaStaffDTO::getMobile));

        map.forEach((mobile, areaStaffData) -> {
            if (areaStaffData.size() > 1) {
                okList.add(areaStaffData.get(0));
                errList.addAll(areaStaffData.stream().skip(1).collect(Collectors.toList()));
            } else {
                okList.addAll(areaStaffData);
            }
        });
        resultMap.put(true, okList);
        resultMap.put(false, errList);
        return resultMap;
    }
}
