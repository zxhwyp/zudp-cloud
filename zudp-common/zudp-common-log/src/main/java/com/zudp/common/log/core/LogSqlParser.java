package com.zudp.common.log.core;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class LogSqlParser {

    public static String logSqlParser(Invocation invocation) {
        MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
        Object parameter = null;
        // 获取参数，if语句成立，表示sql语句有参数，参数格式是map形式
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        // BoundSql就是封装myBatis最终产生的sql类
        BoundSql boundSql = ms.getBoundSql(parameter);
        // 获取节点的配置
        Configuration configuration = ms.getConfiguration();
        // 获取到最终的sql语句
        String sql = showSql(configuration, boundSql);
        return sql;
    }

    public static SqlFragment updateSqlParser(String sql) {
        String sqlCopy = sql.toUpperCase();
        if (sqlCopy.startsWith(LogConfig.DELETE)) {
            return deleteSqlParser(sql);
        }
        SqlFragment sqlFragment = new SqlFragment();
        sqlFragment.act = LogConfig.UPDATE;
        int setIndex = sqlCopy.indexOf(LogConfig.SET);
        String tableFra = sql.substring(LogConfig.UPDATE.length(), setIndex);
        sqlFragment.tables = tableFra;
        sqlFragment.tableEntities = parserTables(tableFra);

        int whereIndex = sqlCopy.indexOf(LogConfig.WHERE);
        sqlFragment.condition = sql.substring(whereIndex + LogConfig.WHERE.length());
        sqlFragment.params = parserParams(sql.substring(setIndex + LogConfig.SET.length(), whereIndex));
        return sqlFragment;
    }

    public static SqlFragment deleteSqlParser(String sql) {
        String sqlCopy = sql.toUpperCase();
        if (sqlCopy.startsWith(LogConfig.UPDATE)) {
            return updateSqlParser(sql);
        }
        SqlFragment sqlFragment = new SqlFragment();
        sqlFragment.act = LogConfig.DELETE;
        int whereIndex = sqlCopy.indexOf(LogConfig.WHERE);
        String tableFra = sql.substring(LogConfig.FROM.length(), whereIndex);
        sqlFragment.tables = tableFra;
        sqlFragment.tableEntities = parserTables(tableFra);
        sqlFragment.condition = sql.substring(whereIndex + LogConfig.WHERE.length());
        return sqlFragment;
    }

    public static List<SqlFragment.Table> parserTables(String fragment) {
        //首先移除收尾空格
        String origin = fragment.trim();
        //更新语句有单表更新和多表更新两种，
        // 先考虑单表更新以及update t1, t2这种形式
        // 还有update t1 join t2 on t1.xx = t2.yy
        String originCopy = origin.toUpperCase();
        if (originCopy.contains(LogConfig.JOIN)) {
            new Throwable("日志解析：暂不支持的sql语句解析");
        }
        List<String> tables = Arrays.stream(origin.split(",")).filter((e) -> e != null && !e.isEmpty()).collect(Collectors.toList());
        List<SqlFragment.Table> results = tables.stream().map((e) -> e.trim())
                .map((e) -> {
                    //此处分两种情况  as指定别名 或者 空格指定别名
                    String eCopy = e.toUpperCase();
                    List<String> tableStrs =  Arrays.asList();
                    if (eCopy.contains(LogConfig.AS)) {
                        tableStrs = sqlSplit(e, LogConfig.AS);
                    }
                    tableStrs = Arrays.asList(e.split(" "));
                    SqlFragment.Table table = new SqlFragment.Table();
                    if (tableStrs.size() > 0) {
                        table.name = tableStrs.get(0);
                        table.alias = tableStrs.get(0);
                    }
                    if (tableStrs.size() > 1) {
                        table.alias = tableStrs.get(1);
                    }
                    return table;
                }).collect(Collectors.toList());
        return results;
    }

    public static List<String> parserParams(String fragment) {
        String origin = fragment.trim();
        List<String> args = Arrays.stream(origin.split(","))
                .map((e) -> e.trim())
                .map((e) -> {
                    String[] params = e.split("=");
                    if (params.length > 0) {
                        return params[0];
                    }else {
                        return "";
                    }
                })
                .filter((e) -> e.length() > 0).collect(Collectors.toList());
        return args;
    }

    //因为sql关键词可以大写也可以小写，统一方法分隔
    public static List<String> sqlSplit(String value ,String split) {
        if (value.contains(split.toUpperCase())) {
            return Arrays.asList(value.split(split.toUpperCase()));
        }
        return Arrays.asList(value.split(split.toLowerCase()));
    }



    // 进行？的替换
    private static String showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // sql语句中多个空格都用一个空格代替
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (CollectionUtils.isNotEmpty(parameterMappings) && parameterObject != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?",
                        Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?",
                                Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 该分支是动态sql
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?",
                                Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        // 打印出缺失，提醒该参数缺失并防止错位
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    // 如果参数是String，则添加单引号， 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                    DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }


}
