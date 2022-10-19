package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.ImportRecord;

/**
 * @author zcb
 */
@DS(Constant.DATA_SOURCE_MYSQL)
public interface StudentImportMapper extends SuperMapper<ImportRecord> {
}
