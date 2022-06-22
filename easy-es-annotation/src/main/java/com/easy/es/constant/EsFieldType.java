package com.easy.es.constant;

public enum EsFieldType {
    //es字段枚举类型
    TEXT,
    BYTE,
    SHORT,
    INTEGER,
    LONG,
    DATE,
    HALF_FLOAT,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    OBJECT,
    AUTO,
    NESTED,
    IP,
    ATTACHMENT,
    KEYWORD,
    //同时创建text和keyword
    STRING;

    public String convert(String fieldType){
        if ("int".equalsIgnoreCase(fieldType)){
            return "integer";
        }
        return fieldType;
    }


}