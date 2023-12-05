package com.zudp.common.log.core;

import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogConfig {

    public static final String SET = "SET";
    public static final String WHERE = "WHERE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String JOIN = "JOIN";
    public static final String AS = "AS";
    public static final String FROM = "FROM";
    public static final String INTO = "INTO";
    public static final String VALUES = "VALUES";

    static public boolean fieldFilter(String field) {
        return !filters.contains(field);
    }

    static private List<String> filters = Arrays.asList("update_time", "create_time", "update_by", "create_by");
}
