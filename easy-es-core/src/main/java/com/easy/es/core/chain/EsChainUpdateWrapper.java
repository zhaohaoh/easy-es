package com.easy.es.core.chain;


import com.easy.es.core.tools.SFunction;
import com.easy.es.core.service.EsService;
import com.easy.es.core.wrapper.EsUpdateWrapper;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class EsChainUpdateWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainUpdateWrapper<T>, EsUpdateWrapper<T>> {
    private final EsService<T> esService;

    public EsChainUpdateWrapper(EsService<T> esService) {
        this.esService = esService;
        Type tClazz = ((ParameterizedType) esService.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        super.tClass = (Class<T>) tClazz;
        super.esWrapper = new EsUpdateWrapper<>(tClass);
    }


    public boolean save(T t) {
        return esService.save(t);
    }

    public boolean update(T t) {
        return esService.updateById(t);
    }

    public List<BulkItemResponse> updateBatch(List<T> t) {
        return esService.updateBatch(t);
    }

    public List<BulkItemResponse> saveBatch(List<T> t) {
        return esService.saveBatch(t);
    }

    public BulkByScrollResponse update() {
        return esService.updateByWrapper(esWrapper);
    }

    public BulkByScrollResponse remove() {
        return esService.remove(esWrapper);
    }


}
