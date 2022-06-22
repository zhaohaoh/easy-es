package com.easy.es.pojo;

import org.elasticsearch.search.aggregations.BucketOrder;
import java.util.HashMap;
import java.util.Map;

public class EsFilterGroupField {
    private String fieldName;
    private Map<String, Object> terms = new HashMap<>();
    private Integer size;
    private BucketOrder bucketOrder = BucketOrder.key(true);

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Map<String, Object> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, Object> terms) {
        this.terms = terms;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public BucketOrder getBucketOrder() {
        return bucketOrder;
    }

    public void setBucketOrder(BucketOrder bucketOrder) {
        this.bucketOrder = bucketOrder;
    }

    @Override
    public String toString() {
        return "EsFilterGroupField{" +
                "fieldName='" + fieldName + '\'' +
                ", terms=" + terms +
                ", size=" + size +
                ", bucketOrder=" + bucketOrder +
                '}';
    }
}
