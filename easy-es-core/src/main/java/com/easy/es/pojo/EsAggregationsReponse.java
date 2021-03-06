package com.easy.es.pojo;

import com.easy.es.core.tools.SFunction;
import com.easy.es.core.wrapper.aggregation.AbstractLambdaAggregationWrapper;
import com.easy.es.exception.EsException;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoTileGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.RareTerms;
import org.elasticsearch.search.aggregations.bucket.terms.RareTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.BucketMetricValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.easy.es.constant.EsConstant.AGG_DELIMITER;


/**
 * @Author: hzh
 * @Date: 2022/6/21 12:31
 */
public class EsAggregationsReponse<T> extends AbstractLambdaAggregationWrapper<T, SFunction<T, ?>> {
    private Aggregations aggregations;

    public void settClass(Class<T> tClass) {
        super.tClass = tClass;
    }

    public Aggregations getAggregations() {
        return aggregations;
    }

    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }

    public Aggregation get(String name) {
        return aggregations.get(name);
    }

    public EsAggregationsReponse<T> getTermSubAggregation(SFunction<T, ?> name) {
        Terms terms = aggregations.get(getAggregationField(name) + AGG_DELIMITER + TermsAggregationBuilder.NAME);
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Aggregations aggregations = bucket.getAggregations();
            EsAggregationsReponse<T> reponse = new EsAggregationsReponse<>();
            reponse.setAggregations(aggregations);
            return reponse;
        }
        throw new EsException("No SubAggregation");
    }

    public Map<String, Long> getTermsAsMap(SFunction<T, ?> name) {
        Terms terms = aggregations.get(getAggregationField(name) + AGG_DELIMITER + TermsAggregationBuilder.NAME);
        Map<String, Long> data = new HashMap<>();
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            data.put(keyAsString, docCount);
        }
        return data;
    }

    public Terms getTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + TermsAggregationBuilder.NAME);
    }

    public RareTerms getRareTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + RareTermsAggregationBuilder.NAME);
    }

    public Filters getFilters(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + FiltersAggregationBuilder.NAME);
    }

    public AdjacencyMatrix getAdjacencyMatrix(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + AdjacencyMatrixAggregationBuilder.NAME);
    }

    public SignificantTerms getSignificantTerms(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + SignificantTermsAggregationBuilder.NAME);
    }

    public Histogram getHistogram(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + HistogramAggregationBuilder.NAME);
    }

    public GeoGrid getGeoGrid(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + GeoTileGridAggregationBuilder.NAME);
    }

    public Max getMax(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + MaxAggregationBuilder.NAME);
    }

    public Avg getAvg(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + AvgAggregationBuilder.NAME);
    }

    public Sum getSum(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + SumAggregationBuilder.NAME);
    }

    public ValueCount getCount(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + SumAggregationBuilder.NAME);
    }

    public WeightedAvg getWeightedAvg(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + SumAggregationBuilder.NAME);
    }

    public BucketMetricValue getBucketMetricValue(SFunction<T, ?> name) {
        return aggregations.get(getAggregationField(name) + AGG_DELIMITER + SumAggregationBuilder.NAME);
    }
}
