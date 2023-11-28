package com.zudp.common.log.interceptor;

import com.zudp.common.log.annotation.Log;
import com.zudp.common.log.aspect.LogAspect;
import com.zudp.common.log.core.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),})
@Component
public class LogInterceptor implements Interceptor {

    @Resource
    LogManager logManager;
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
            Log logEntity = LogAspect.getLocalLog().getLog();
            String[] ids = new String[]{};
            if (logEntity != null) {
                ids = Optional.ofNullable(logEntity.sqlId()).orElse(ids);
            }
            Boolean isMatch = Arrays.asList(ids).contains(ms.getId());
            if (isMatch) {
                logManager.logHandle(invocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 执行完上面的任务后，不改变原有的sql执行过程
        return invocation.proceed();
    }

}
