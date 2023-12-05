package com.zudp.common.log.aspect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.zudp.common.log.annotation.Log;
import com.zudp.common.log.core.JdbcManager;
import com.zudp.common.log.core.logparser.MultTableLogParser;
import com.zudp.common.log.core.logparser.SingleTableLogParser;
import com.zudp.common.log.core.logparser.TableLogParser;
import com.zudp.common.log.enums.BusinessType;
import com.zudp.common.log.pojo.LogWraper;
import com.zudp.common.log.pojo.TableColumn;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.zudp.common.core.utils.ServletUtils;
import com.zudp.common.core.utils.StringUtils;
import com.zudp.common.core.utils.ip.IpUtils;
import com.zudp.common.log.enums.BusinessStatus;
import com.zudp.common.log.filter.PropertyPreExcludeFilter;
import com.zudp.common.log.service.AsyncLogService;
import com.zudp.common.security.utils.SecurityUtils;
import com.zudp.system.api.domain.SysOperLog;

/**
 * 操作日志记录处理
 *
 * @author zudp
 */
@Aspect
@Component
public class LogAspect
{
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 排除敏感属性字段 */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    /** 计算操作消耗时间 */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");

    private static final TransmittableThreadLocal<LogWraper> LOG_THREADLOCAL = new TransmittableThreadLocal<>();

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    JdbcManager jdbcManager;

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void boBefore(JoinPoint joinPoint, Log controllerLog)
    {
        MethodSignature ms = (MethodSignature)joinPoint.getSignature();
        LOG_THREADLOCAL.set(LogWraper.builder()
                .log(controllerLog)
                .method(ms.getMethod())
                .build());
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult)
    {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e)
    {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult)
    {
        try
        {
            // *========数据库日志=========*//
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = IpUtils.getIpAddr();
            operLog.setOperIp(ip);
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            String username = SecurityUtils.getUsername();
            if (StringUtils.isNotBlank(username))
            {
                operLog.setOperName(username);
            }

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 设置消耗时间
            operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            if (e != null)
            {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            } else {
                //添加详细日志记录
                try {
                    packageContent(joinPoint, controllerLog, operLog);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
            // 保存数据库
            asyncLogService.saveSysLog(operLog);
        }
        catch (Exception exp)
        {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
        finally
        {
            TIME_THREADLOCAL.remove();
            LOG_THREADLOCAL.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log 日志
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) throws Exception
    {
        // 设置action动作
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operLog.setContent(log.content());
        // 设置操作人类别
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData())
        {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult))
        {
            operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog, String[] excludeParamNames) throws Exception
    {
        String requestMethod = operLog.getRequestMethod();
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap)
                && (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)))
        {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operLog.setOperParam(StringUtils.substring(params, 0, 2000));
        }
        else
        {
            operLog.setOperParam(StringUtils.substring(JSON.toJSONString(paramsMap, excludePropertyPreFilter(excludeParamNames)), 0, 2000));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames)
    {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o) && !isFilterObject(o))
                {
                    try
                    {
                        String jsonObj = JSON.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params += jsonObj.toString() + " ";
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 忽略敏感属性
     */
    public PropertyPreExcludeFilter excludePropertyPreFilter(String[] excludeParamNames)
    {
        return new PropertyPreExcludeFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o)
    {
        Class<?> clazz = o.getClass();
        if (clazz.isArray())
        {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        else if (Collection.class.isAssignableFrom(clazz))
        {
            Collection collection = (Collection) o;
            for (Object value : collection)
            {
                return value instanceof MultipartFile;
            }
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            Map map = (Map) o;
            for (Object value : map.entrySet())
            {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

    protected static void setLocalPage(LogWraper log) {
        LOG_THREADLOCAL.set(log);
    }

    public static LogWraper getLocalLog() {
        return Optional.ofNullable(LOG_THREADLOCAL.get()).orElse(LogWraper.builder().build());
    }

    private void packageContent(final JoinPoint joinPoint, Log controllerLog, SysOperLog operLog) {
        String content = "";
        if (controllerLog.businessType() == BusinessType.INSERT) {
            content = packageInsertContent(joinPoint, controllerLog);
        }else {
            content = packageOtherContent(controllerLog);
        }
        StringBuilder str = new StringBuilder();
        if (StringUtils.isNotEmpty(operLog.getContent())) {
            str.append(operLog.getContent());
        }else {
            str.append("内容");
        }
        if (StringUtils.isNotEmpty(content)) {
            str.append(String.format("：%s", content));
        }
        operLog.setContent(str.toString());
    }
    private String packageInsertContent(final JoinPoint joinPoint, Log controllerLog) {
        String nameKey = controllerLog.nameKey();
        //字段
        List<String> params = LogAspect.getLocalLog().getParams();
        //值
        List<List<String>> values = LogAspect.getLocalLog().getValues();
        int index = params.indexOf(nameKey);
        //有可能用户传过来的是驼峰转成下划线试下
        if (index == -1) {
            index = params.indexOf(StrUtil.toUnderlineCase(nameKey));
        }
        StringBuilder content = new StringBuilder("");
        if (index == -1) {
            return content.toString();
        }
        final int finalIndex = index;
        values.stream().forEach((e) -> {
            int i = values.indexOf(e);
            if (i == 0) {
                content.append(e.get(finalIndex));
            }else {
                content.append(String.format(",%s", e.get(finalIndex)));
            }
        });
        return content.toString();
    }

    private String packageOtherContent(Log controllerLog) {
        if (controllerLog.businessType() == BusinessType.UPDATE) {
            String selectSql = LogAspect.getLocalLog().getSelectSql();
            List<Map<String, Object>> result = jdbcManager.queryWithSql(selectSql);
            LogAspect.getLocalLog().setNewValues(result);
        }
        Map<String, List<TableColumn>> meta = jdbcManager.queryWithTableMetaData(LogAspect.getLocalLog().getTables());
        LogAspect.getLocalLog().setMetaData(meta);
        TableLogParser logParser = new SingleTableLogParser();
        if (LogAspect.getLocalLog().getTables().size() > 1) {
            logParser = new MultTableLogParser();
        }
        String ctn = logParser.parser(LogAspect.getLocalLog());
        return ctn;
    }

}
