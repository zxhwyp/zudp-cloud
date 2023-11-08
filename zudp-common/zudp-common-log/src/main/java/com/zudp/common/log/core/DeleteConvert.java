package com.zudp.common.log.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteConvert implements SqlConvert{
    @Override
    public String doSqlConvert(String sql) {
        log.info("-------删除转换语句{}", sql);
        return "删除";
    }
}
