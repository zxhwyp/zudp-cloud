package com.zudp.common.log.core.logparser;
import cn.hutool.core.util.StrUtil;
import com.zudp.common.log.annotation.Log;
import com.zudp.common.log.core.LogConfig;
import com.zudp.common.log.enums.BusinessType;
import com.zudp.common.log.pojo.ChangeRecord;
import com.zudp.common.log.pojo.LogWraper;
import com.zudp.common.log.pojo.TableColumn;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
        Log logN = logWraper.getLog();
        if (logN.businessType() == BusinessType.DELETE) {
            return deleteParser(logWraper, columnsMap);
        }
        return updateParser(logWraper, columnsMap);
    }

    private String updateParser(LogWraper logWraper, Map columnsMap) {
        Log logN = logWraper.getLog();
        List<Map<String, Object>> oldValues = logWraper.getOldValues();
        List<Map<String, Object>> newValues = logWraper.getNewValues();
        List<ChangeRecord> changes = new ArrayList<>();
        oldValues.stream().forEach((e) -> {
            int index = oldValues.indexOf(e);
            Map updateValue = newValues.get(index);
            ChangeRecord changeRecord = new ChangeRecord();
            String nameKey = logN.nameKey();
            Object nameObValue = updateValue.get(nameKey);
            if (nameObValue == null) {
                //用下划线试试
                nameObValue = updateValue.get(StrUtil.toUnderlineCase(nameKey));
            }
            String nameValue = "";
            if (nameObValue != null) {
                nameValue = nameObValue.toString();
            }
            changeRecord.setNameValue(nameValue);
            List<ChangeRecord.ColumnRecord> records = new ArrayList<>();
            e.forEach((key, oldValue) -> {
                Object newValue = updateValue.get(key);
                String newValueStr = Optional.ofNullable(newValue).orElse("空").toString();
                String oldValueStr = Optional.ofNullable(oldValue).orElse("空").toString();
                if (newValueStr.equals(oldValueStr) == false && LogConfig.fieldFilter(key)) {
                    records.add(ChangeRecord.ColumnRecord.builder()
                            .oldValue(oldValueStr)
                            .comment(columnsMap.get(key).toString())
                            .newValue(newValueStr)
                            .param(key)
                            .build());
                }
            });
            changeRecord.setRecords(records);
            changes.add(changeRecord);
        });
        StringBuilder contentStr = new StringBuilder();
        changes.stream().forEach((e) -> {
            int index = changes.indexOf(e);
            if (index != 0) {
                contentStr.append("\n");
            }
            contentStr.append(e.getNameValue() + "->");
            e.getRecords().stream().forEach((filed) -> {
                contentStr.append(String.format("%s：由【%s】改成【%s】,", filed.getComment(), filed.getOldValue(), filed.getNewValue()));
            });
        });
        return contentStr.toString();
    }
    private String deleteParser(LogWraper logWraper, Map columnsMap) {
        List<Map<String, Object>> oldValues = logWraper.getOldValues();
        Log logN = logWraper.getLog();
        StringBuilder contentStr = new StringBuilder();
        oldValues.stream().forEach((e) -> {
            int index = oldValues.indexOf(e);
            if (index != 0) {
                contentStr.append(",");
            }
            String nameKey = logN.nameKey();
            Object nameObValue = e.get(nameKey);
            if (nameObValue == null) {
                //用下划线试试
                nameObValue = e.get(StrUtil.toUnderlineCase(nameKey));
            }
            if (nameObValue != null) {
                String nameValue = nameObValue.toString();
                contentStr.append(nameValue);
            }
        });
        return contentStr.toString();
    }
}
