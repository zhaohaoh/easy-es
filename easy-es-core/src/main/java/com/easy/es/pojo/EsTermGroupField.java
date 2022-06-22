package com.easy.es.pojo;

import org.elasticsearch.search.aggregations.BucketOrder;


public class EsTermGroupField {
    private String fieldName;
    private Integer size;
    private BucketOrder bucketOrder = BucketOrder.key(true);

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
        return "EsTermGroupField{" +
                "fieldName='" + fieldName + '\'' +
                ", size=" + size +
                ", bucketOrder=" + bucketOrder +
                '}';
    }
}
