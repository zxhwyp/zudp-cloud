package com.zudp.common.log.core;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlFragment {
    //指令：update、delete等
    String act;

    //查询条件
    String condition;

    //字段
    List<String> params;

    String selectSql;

    String  getSqlParams()  {
        String result = params.stream().collect(Collectors.joining(","));
         return result;
    }

    //表
    String tables;

    List<Table> tableEntities;

    @Data
    static class Table {
        String name;
        String alias;
    }
}
