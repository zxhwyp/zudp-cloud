package com.zudp.common.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.zudp.common.log.enums.BusinessType;
import com.zudp.common.log.enums.OperatorType;

/**
 * 自定义操作日志记录注解
 *
 * @author zudp
 *
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log
{

    /**
     *  查询的sql id  Mapper的方法签名
     */
    public String[] sqlId() default {};

    /**
     *  表对象的名字字段，如用户的name字段
     */
    public String nameKey() default "";

    /**
     * 操作内容
     */
    public String content() default "";

    /**
     * 功能
     */
    public BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
    public OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
    public boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    public boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数
     */
    public String[] excludeParamNames() default {};
}
