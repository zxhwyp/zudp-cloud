package com.zudp.common.log.core.sqlparser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InsertConvert implements SqlConvert {
    @Override
    public SqlFragment doSqlConvert(String sql) {
        SqlFragment fragment = LogSqlParser.insertSqlParser(sql);
        return fragment;
    }
}
