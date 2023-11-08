package com.zudp.common.log.core;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.stereotype.Component;

@Component
public class LogManager {

    public static void logHandle(Invocation invocation) {
        //先解析出原sql
        String sql = LogSqlParser.logSqlParser(invocation);
        //按sql类型转换成select语句
        MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
        SqlCommandType commandType = ms.getSqlCommandType();
        SqlConvert sqlConvert = null;
        switch (commandType) {
            case UPDATE:
                sqlConvert = new UpdateConvert();
                break;
            case DELETE:
                sqlConvert = new DeleteConvert();
                break;
        }
        if (sqlConvert == null) {
            return;
        }
        sqlConvert.doSqlConvert(sql);
    }
}
