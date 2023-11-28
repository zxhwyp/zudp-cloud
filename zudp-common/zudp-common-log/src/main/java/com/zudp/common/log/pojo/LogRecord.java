package com.zudp.common.log.pojo;

import lombok.Data;

@Data
public class LogRecord {
    String param;
    String comment;
    String oldValue;
    String newValue;
}
