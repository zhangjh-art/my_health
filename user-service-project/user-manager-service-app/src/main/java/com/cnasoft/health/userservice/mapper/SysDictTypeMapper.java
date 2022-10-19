package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.SysDictType;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lqz
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Mapper
public interface SysDictTypeMapper extends SuperMapper<SysDictType> {

}
