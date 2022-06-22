package com.easy.es.core.wrapper;

import com.easy.es.pojo.EsHighLight;
import com.easy.es.pojo.EsOrder;
import com.easy.es.pojo.EsSelect;
import com.easy.es.pojo.EsUpdateField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;

import java.util.List;

public abstract class EsWrapper<T> {
    public abstract List<EsOrder> getEsOrderList();

    public abstract EsSelect getEsSelect();

    public abstract EsSelect getSelect();

    public abstract EsHighLight getEsHighLight();

    public abstract BoolQueryBuilder getQueryBuilder();

    public abstract List<AggregationBuilder> getAggregationBuilder();

    public abstract List<PipelineAggregationBuilder> getPipelineAggregatorBuilders();

    public EsUpdateField getEsUpdateField() {
        return null;
    }
}
