package com.easy.es.core.wrapper;


import com.easy.es.core.tools.SFunction;
import com.easy.es.core.wrapper.aggregation.EsAggregationWrapper;

public class EsQueryWrapper<T> extends  AbstractEsWrapper<T, SFunction<T, ?>, EsQueryWrapper<T>> {


    /**
     * 可自动映射keyword  建议使用
     *
     * @param tClass
     */
    public EsQueryWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    @Override
    protected EsQueryWrapper<T> instance() {
        if (super.tClass != null) {
            return new EsQueryWrapper<>(super.tClass);
        }
        return new EsQueryWrapper<>(super.tClass);
    }


    public Class<T> gettClass() {
        return super.tClass;
    }
}
