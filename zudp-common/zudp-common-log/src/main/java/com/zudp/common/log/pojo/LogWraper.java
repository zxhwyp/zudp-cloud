package com.zudp.common.log.pojo;

import com.zudp.common.log.annotation.Log;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogWraper {

   private Log log;

   private Method method;

   private String selectSql;

   //字段
   List<String> params;

   //值
   List<List<String>> values;

   //涉及到的表
   private List<String> tables;

   //执行前的值
   private List<Map<String, Object>> oldValues;
   //执行后的值
   private List<Map<String, Object>> newValues;
   //表元数据
   private Map<String, List<TableColumn>> metaData;

}
