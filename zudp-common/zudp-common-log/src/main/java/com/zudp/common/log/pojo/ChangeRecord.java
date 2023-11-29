package com.zudp.common.log.pojo;

import com.zudp.common.core.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRecord {
    String nameValue;
    List<ColumnRecord> records;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnRecord {
        String param;
        String comment;
        String oldValue;
        String newValue;

        public String getComment() {
            if (StringUtils.isEmpty(comment)) {
                return "缺少字段注释";
            }
            return comment;
        }
        public String getOldValue() {
            if (StringUtils.isEmpty(oldValue)) {
                return "空";
            }
            return oldValue;
        }
        public String getNewValue() {
            if (StringUtils.isEmpty(newValue)) {
                return "空";
            }
            return newValue;
        }
    }
}

