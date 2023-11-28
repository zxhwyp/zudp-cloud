package com.zudp.common.log.core;

import com.zudp.common.log.aspect.LogAspect;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LogManager {

    @Autowired
    JdbcManager jdbcManager;

    public void logHandle(Invocation invocation) {
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
        SqlFragment fragment = sqlConvert.doSqlConvert(sql);
        List<Map<String, Object>> result = jdbcManager.queryWithSql(fragment.selectSql);
        LogAspect.getLocalLog().setAct(fragment.getAct());
        LogAspect.getLocalLog().setSelectSql(fragment.selectSql);
        LogAspect.getLocalLog().setOldValues(result);
        LogAspect.getLocalLog().setTables(fragment.tableEntities.stream().map((e) -> e.name).collect(Collectors.toList()));
    }
}
