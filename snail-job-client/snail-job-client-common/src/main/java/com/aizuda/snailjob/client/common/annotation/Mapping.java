package com.aizuda.snailjob.client.common.annotation;

import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;

import java.lang.annotation.*;

/**
 * 接口定义
 *
 * @author: opensnail
 * @date : 2023-05-11 22:32
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    /**
     * 请求类型
     */
    RequestMethod method() default RequestMethod.POST;

    /**
     * 请求路径
     */
    String path() default "";

}
