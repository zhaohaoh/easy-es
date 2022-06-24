package com.easy.es.core.wrapper.aggregation;


import com.easy.es.core.tools.SFunction;
import com.easy.es.core.wrapper.AbstractLambdaEsWrapper;
import com.easy.es.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractLambdaAggregationWrapper<T, R extends SFunction<T, ?>> extends AbstractLambdaEsWrapper<T, R> {

    protected Class<T> tClass;

    public String getAggregationField(R sFunction) {
        String name = nameToString(sFunction);
        String keyword = EsParamHolder.getStringKeyword(tClass, name);
        return StringUtils.isBlank(keyword) ? name : keyword;
    }

}
