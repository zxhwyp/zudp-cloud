package com.zudp.common.log.core;

import com.zudp.common.log.aspect.LogAspect;
import com.zudp.common.log.core.sqlparser.*;
import com.zudp.common.log.enums.BusinessType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.beans.factory.annotation.Autowired;
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
        BusinessType type = LogAspect.getLocalLog().getLog().businessType();
        SqlConvert sqlConvert = null;
        switch (type) {
            case UPDATE:
                sqlConvert = new UpdateConvert();
                break;
            case DELETE:
                sqlConvert = new DeleteConvert();
                break;
            case INSERT:
                sqlConvert = new InsertConvert();
                break;
        }
        if (sqlConvert == null) {
            return;
        }
        SqlFragment fragment = sqlConvert.doSqlConvert(sql);
        if (type == BusinessType.INSERT) {
            LogAspect.getLocalLog().setParams(fragment.getParams());
            LogAspect.getLocalLog().setValues(fragment.getValues());
        }else {
            List<Map<String, Object>> result = jdbcManager.queryWithSql(fragment.getSelectSql());
            LogAspect.getLocalLog().setSelectSql(fragment.getSelectSql());
            LogAspect.getLocalLog().setOldValues(result);
            LogAspect.getLocalLog().setTables(fragment.getTableEntities().stream().map((e) -> e.getName()).collect(Collectors.toList()));
        }
    }
}
