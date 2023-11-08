package com.zudp.common.log.pojo;

import com.zudp.common.log.annotation.Log;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogWraper {

   private Log log;

   private Method method;

}
