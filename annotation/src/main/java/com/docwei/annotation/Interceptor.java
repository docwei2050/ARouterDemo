package com.docwei.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Interceptor {
    //越大优先级越高，必须要写优先级，不然可能会存在重复优先级的拦截器，会报错
    int priority() default 0;
}
