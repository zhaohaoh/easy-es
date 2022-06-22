package com.easy.es.core.service;


import com.easy.es.core.ScrollHandler;
import com.easy.es.core.chain.EsChainQueryWrapper;
import com.easy.es.core.chain.EsChainUpdateWrapper;
import com.easy.es.core.wrapper.EsQueryWrapper;
import com.easy.es.core.wrapper.EsUpdateWrapper;
import com.easy.es.core.wrapper.EsWrapper;
import com.easy.es.pojo.EsResponse;
import com.easy.es.pojo.EsSettings;
import com.easy.es.pojo.PageInfo;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface EsService<T> {

    EsQueryWrapper<T> esQueryWrapper();

    EsUpdateWrapper<T> esUpdateWrapper();

    EsChainQueryWrapper<T> esChainQueryWrapper();

    EsChainUpdateWrapper<T> esChainUpdateWrapper();

    void createIndex();

    void createIndexMapping();

    void createMapping();

    boolean updateSettings(EsSettings esSettings);

    boolean save(T entity);

    boolean saveOrUpdate(T entity);

    List<BulkItemResponse> saveBatch(Collection<T> entityList);

    List<BulkItemResponse> saveBatch(Collection<T> entityList, int batchSize);

    boolean removeById(Serializable id);

    boolean removeByIds(Collection<? extends Serializable> idList);

    BulkByScrollResponse remove(EsWrapper<T> esUpdateWrapper);

    BulkByScrollResponse removeAll();

    boolean updateById(T entity);

    List<BulkItemResponse> updateBatch(Collection<T> entityList);

    List<BulkItemResponse> updateBatch(Collection<T> entityList, int batchSize);

    void deleteIndex();

    BulkByScrollResponse updateByWrapper(EsWrapper<T> esUpdateWrapper);

    T getById(String id);

    List<T> listByIds(Collection<String> idList);

    EsResponse<T> list(EsQueryWrapper<T> esQueryWrapper);

    EsResponse<T> page(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper);

    long count(EsQueryWrapper<T> esQueryWrapper);

    default void scroll(EsQueryWrapper<T> esQueryWrapper, int size, ScrollHandler<T> scrollHandler) {
        scroll(esQueryWrapper, size, 1, scrollHandler);
    }

    void scroll(EsQueryWrapper<T> esQueryWrapper, int size, int keepTime, ScrollHandler<T> scrollHandler);
}
