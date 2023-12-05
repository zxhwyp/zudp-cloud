package com.zudp.common.log.core.sqlparser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteConvert implements SqlConvert {
    @Override
    public SqlFragment doSqlConvert(String sql) {
        SqlFragment fragment = LogSqlParser.deleteSqlParser(sql);
        fragment.setSelectSql(String.format("SELECT * FROM %s WHERE %s", fragment.getTables(), fragment.getCondition()));
        return fragment;
    }
}
