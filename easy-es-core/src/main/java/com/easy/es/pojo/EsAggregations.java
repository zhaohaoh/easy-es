package com.easy.es.pojo;

import lombok.Data;

import java.util.List;

@Data
public class EsAggregations {
    private List<EsAggregation> bucket;
}
