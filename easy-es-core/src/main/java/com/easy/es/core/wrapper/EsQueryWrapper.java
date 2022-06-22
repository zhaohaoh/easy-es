package com.easy.es.core.wrapper;

public class EsQueryWrapper<T> extends AbstractLambdaEsWrapper<T, EsQueryWrapper<T>> {

    public EsQueryWrapper() {
    }

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
        return new EsQueryWrapper<>();
    }

}
