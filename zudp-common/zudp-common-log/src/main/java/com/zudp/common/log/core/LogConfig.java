package com.zudp.common.log.core;

import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogConfig {

    public static final String SET = "SET";
    public static final String WHERE = "WHERE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String JOIN = "JOIN";
    public static final String AS = "AS";
    public static final String FROM = "FROM";

    static public boolean fieldFilter(String field) {
        return !filters.contains(field);
    }

    static private List<String> filters = Arrays.asList("update_time", "create_time");
}
