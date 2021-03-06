package com.easy.es.pojo;


import org.elasticsearch.search.SearchHits;

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
    private EsAggregationsReponse<T> esAggregationsReponse;

    private Map<String, SearchHits> topHits;

    public EsResponse(List<T> list, long count, EsAggregationsReponse<T> esAggregationReponse) {
        this.list = list;
        this.total = count;
        this.esAggregationsReponse = esAggregationReponse;
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

    public EsAggregationsReponse<T> getEsAggregationsReponse() {
        return esAggregationsReponse;
    }

    public void setEsAggregationsReponse(EsAggregationsReponse<T> esAggregationsReponse) {
        this.esAggregationsReponse = esAggregationsReponse;
    }

    public Map<String, SearchHits> getTopHits() {
        return topHits;
    }

    public void setTopHits(Map<String, SearchHits> topHits) {
        this.topHits = topHits;
    }
}
