package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.StudentAdditionalInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Administrator
 */
@DS(Constant.DATA_SOURCE_MYSQL)
public interface StudentAdditionalInfoMapper extends SuperMapper<StudentAdditionalInfo> {

    /**
     * 批量插入学生补充信息
     *
     * @param additionalInfos 学生补充信息列表
     * @param key             秘钥
     * @return 行数
     */
    @Override
    int insertBatch(@Param("list") List<StudentAdditionalInfo> additionalInfos, @Param("key") String key);

    /**
     * 批量更新学生补充信息
     *
     * @param additionalInfos 学生补充信息列表
     * @param key             秘钥
     * @return 行数
     */
    @Override
    int updateBatch(@Param("list") List<StudentAdditionalInfo> additionalInfos, @Param("key") String key);
}
