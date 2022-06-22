package com.easy.es.pojo;

import lombok.Data;

@Data
public class EsAggregation {
    private EsAggregations aggregations;
    private String name;
    private Long docCount;
    private Double value;
}
