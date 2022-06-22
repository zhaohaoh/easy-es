package com.easy.es.constant;

public interface EsConstant {
    /*
      INDEX属性
     */
    String NUMBER_OF_SHARDS = "number_of_shards";
    String NUMBER_OF_REPLICAS = "number_of_replicas";
    /* 字段属性
     */
    String TYPE = "type";
    String ANALYZER = "analyzer";
    String SEARCH_ANALYZER = "search_analyzer";
    String STORE = "store";
    String INDEX = "index";
    String FIELDS = "fields";
    //代表keyword字符串有效搜索长度
    String IGNORE_ABOVE = "ignore_above";
}
