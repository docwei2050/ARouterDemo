package com.docwei.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//编译器才需要的，主要是生成新的类
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface AutoWird {

}
