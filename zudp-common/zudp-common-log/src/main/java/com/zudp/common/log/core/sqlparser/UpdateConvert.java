package com.zudp.common.log.core.sqlparser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateConvert  implements SqlConvert {
    @Override
    public SqlFragment doSqlConvert(String sql) {
        SqlFragment fragment = LogSqlParser.updateSqlParser(sql);
        fragment.setSelectSql(String.format("SELECT %s FROM %s WHERE %s", fragment.getSqlParams(), fragment.getTables(), fragment.getCondition()));
        return fragment;
    }
}
