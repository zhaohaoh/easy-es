package com.easy.es.pojo;

import org.elasticsearch.search.aggregations.BucketOrder;


public class EsRangeGroupField {
    private String fieldName;
    private Integer size;
    //最低下线
    private Double from;
    //最高上线
    private Double to;
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

    public Double getFrom() {
        return from;
    }

    public void setFrom(Double from) {
        this.from = from;
    }

    public Double getTo() {
        return to;
    }

    public void setTo(Double to) {
        this.to = to;
    }

    public BucketOrder getBucketOrder() {
        return bucketOrder;
    }

    public void setBucketOrder(BucketOrder bucketOrder) {
        this.bucketOrder = bucketOrder;
    }

    @Override
    public String toString() {
        return "EsRangeGroupField{" +
                "fieldName='" + fieldName + '\'' +
                ", size=" + size +
                ", from=" + from +
                ", to=" + to +
                ", bucketOrder=" + bucketOrder +
                '}';
    }
}
