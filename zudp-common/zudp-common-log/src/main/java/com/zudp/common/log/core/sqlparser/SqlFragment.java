package com.zudp.common.log.core.sqlparser;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlFragment {

    //查询条件
    String condition;

    //字段
    List<String> params;

    //值
    List<List<String>> values;

    String selectSql;

    public String  getSqlParams()  {
        String result = params.stream().collect(Collectors.joining(","));
         return result;
    }

    //表
    String tables;

    List<Table> tableEntities;

    @Data
    public static class Table {
        String name;
        String alias;
    }
}
