package com.easy.es.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsIndex {

    String index();

    String type() default "_doc";

    int shard() default 5;

    int replices() default 1;

    //初始窗口值 可更改
    int initMaxResultWindow() default 100000;

    //初始刷新值 可更改
    String initRefreshInterval() default "1s";

    String[] analyzer() default {};

    String defaultAnalyzer() default "";
}
