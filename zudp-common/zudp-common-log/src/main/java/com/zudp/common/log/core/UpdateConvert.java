package com.zudp.common.log.core;

import com.sun.javafx.binding.StringFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateConvert  implements SqlConvert {
    @Override
    public SqlFragment doSqlConvert(String sql) {
        SqlFragment fragment = LogSqlParser.updateSqlParser(sql);
        if (fragment.act.equals("DELETE")) {
            fragment.selectSql = String.format("SELECT * FROM %s WHERE %s", fragment.tables, fragment.condition);
        }
        fragment.selectSql =  String.format("SELECT %s FROM %s WHERE %s", fragment.getSqlParams(), fragment.tables, fragment.condition);
        return fragment;
    }
}
