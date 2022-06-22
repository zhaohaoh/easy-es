package com.easy.es.annotation;

import com.easy.es.constant.EsFieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsField {
    String name() default "";

    EsFieldType type() default EsFieldType.AUTO;

    boolean index() default true;

    String searchAnalyzer() default "";

    String analyzer() default "";

    boolean store() default false;

    boolean exist() default true;
}
