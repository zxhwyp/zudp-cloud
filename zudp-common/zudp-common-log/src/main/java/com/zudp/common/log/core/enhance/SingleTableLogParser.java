package com.zudp.common.log.core.enhance;
import com.zudp.common.log.annotation.Log;
import com.zudp.common.log.pojo.LogWraper;
import com.zudp.common.log.pojo.TableColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SingleTableLogParser implements TableLogParser {
    @Override
    public String parser(LogWraper logWraper) {
        Map<String, List<TableColumn>> metaData = logWraper.getMetaData();
        Object[] tables = metaData.values().toArray();
        if (tables == null || tables.length == 0) {
            return "";
        }
        List<TableColumn> tableColumns = (List<TableColumn>) tables[0];
        Map columnsMap = tableColumns.stream().collect(Collectors.toMap(TableColumn::getColumnName, TableColumn::getColumnComment));
        if (logWraper.getAct().equals("DELETE")) {
            return deleteParser(logWraper, columnsMap);
        }
        return updateParser(logWraper, columnsMap);
    }

    private String updateParser(LogWraper logWraper, Map columnsMap) {
        Log log = logWraper.getLog();
        List<Map<String, Object>> oldValues = logWraper.getOldValues();
        List<Map<String, Object>> newValues = logWraper.getNewValues();


        return null;
    }
    private String deleteParser(LogWraper logWraper, Map columnsMap) {

        return null;
    }
}
