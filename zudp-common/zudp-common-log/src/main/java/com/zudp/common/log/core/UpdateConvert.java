package com.zudp.common.log.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateConvert  implements SqlConvert {
    @Override
    public String doSqlConvert(String sql) {
        log.info("-------更新转换语句{}", sql);
        return "更新";
    }

}
