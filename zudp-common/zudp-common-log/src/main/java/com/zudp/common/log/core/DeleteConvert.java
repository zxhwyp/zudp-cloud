package com.zudp.common.log.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteConvert implements SqlConvert{
    @Override
    public SqlFragment doSqlConvert(String sql) {
        SqlFragment fragment = LogSqlParser.deleteSqlParser(sql);
        if (fragment.act.equals("UPDATE")) {
            fragment.selectSql = String.format("SELECT %s FROM %s WHERE %s", fragment.getSqlParams(), fragment.tables, fragment.condition);
        }
        fragment.selectSql =  String.format("SELECT * FROM %s WHERE %s", fragment.tables, fragment.condition);
        return fragment;
    }
}
