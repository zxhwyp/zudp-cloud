package com.zudp.common.log.core;

import com.zudp.common.log.pojo.TableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JdbcManager {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> queryWithSql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    //查询数据库元信息
    public Map<String, List<TableColumn>> queryWithTableMetaData(List<String> tables) {
        Map<String, List<TableColumn>> tableColumnMap = new HashMap();
        String base = "SELECT column_name, column_comment FROM\tinformation_schema.COLUMNS WHERE table_schema = (SELECT DATABASE()) AND table_name = '%s' ORDER BY ordinal_position";
        tables.stream().forEach((e) -> {
           String sql = String.format(base, e);
            List<TableColumn> columns = jdbcTemplate.queryForList(sql, TableColumn.class);
            tableColumnMap.put(e, columns);
        });
        return tableColumnMap;
    }
}
