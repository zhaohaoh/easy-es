package com.easy.es.pojo;


import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EsResponse<T> {
    /**
     * 数据集合
     */
    private List<T> list;

    /**
     * 总数
     */
    private long total;
    /**
     * 聚合结果
     */
    private Aggregations aggregations;

    private Map<String, SearchHits> topHits;

    public EsResponse(List<T> list, long count, Aggregations aggregations) {
        this.list = list;
        this.total = count;
        this.aggregations = aggregations;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Aggregations getAggregations() {
        return aggregations;
    }

    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }

    //key为字段值分组的结果 value为count统计的数量  传入聚合名称获取
    public EsAggregations resolveAggregations() {
        return getEsAggregation(aggregations);
    }

    private EsAggregations getEsAggregation(Aggregations aggregations) {
        EsAggregations esAggregations = new EsAggregations();
        List<EsAggregation> esAggregationsList = new ArrayList<>();
        esAggregations.setBucket(esAggregationsList);
        for (Aggregation aggregation : aggregations) {
            if (aggregation instanceof NumericMetricsAggregation.SingleValue) {
                NumericMetricsAggregation.SingleValue metricsAggregation = (NumericMetricsAggregation.SingleValue) aggregation;
                String name = metricsAggregation.getName();
                double value = metricsAggregation.value();
                EsAggregation esAggregation = new EsAggregation();
                esAggregation.setName(name);
                esAggregation.setValue(value);
                esAggregationsList.add(esAggregation);
            } else if (aggregation instanceof Terms) {
                for (Terms.Bucket bucket : ((Terms) aggregation).getBuckets()) {
                    EsAggregation esAggregation = new EsAggregation();
                    esAggregation.setName(bucket.getKeyAsString());
                    esAggregation.setDocCount(bucket.getDocCount());
                    Aggregations bucketAggregations = bucket.getAggregations();
                    if (bucketAggregations != null && bucketAggregations.asList().size() > 0) {
                        EsAggregations esAggregation1 = getEsAggregation(bucketAggregations);
                        esAggregation.setAggregations(esAggregation1);
                    }
                    esAggregationsList.add(esAggregation);
                }
            } else if (aggregation instanceof ParsedTopHits) {
                SearchHits hits = ((ParsedTopHits) aggregation).getHits();
                if (topHits == null) {
                    topHits = new HashMap<>();
                }
                topHits.put(aggregation.getName(), hits);
            }
        }
        return esAggregations;
    }

    //key为字段值分组的结果。值是long类型的话分组结果是Longterms表示 value为count统计的数量  传入聚合名称获取
    public Map<Long, Object> getLongBucket(String name) {
        Map<Long, Object> data = new HashMap<>();
        ParsedLongTerms aggregation = aggregations.get(name);
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            //key的分组后的每个字段名，docCount是数量
            data.put(bucket.getKeyAsNumber().longValue(), bucket.getDocCount());
        }
        return data;
    }


    @Override
    public String toString() {
        return "EsResponse{" +
                "list=" + list +
                ", total=" + total +
                ", aggregations=" + aggregations +
                '}';
    }
}
