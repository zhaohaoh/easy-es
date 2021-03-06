package com.easy.es.core.service;

import com.easy.es.core.ScrollHandler;
import com.easy.es.core.chain.EsChainQueryWrapper;
import com.easy.es.core.chain.EsChainUpdateWrapper;
import com.easy.es.core.wrapper.EsQueryWrapper;
import com.easy.es.core.wrapper.EsUpdateWrapper;
import com.easy.es.core.wrapper.EsWrapper;
import com.easy.es.pojo.EsAggregationsReponse;
import com.easy.es.pojo.EsResponse;
import com.easy.es.pojo.EsSettings;
import com.easy.es.pojo.PageInfo;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
@SuppressWarnings({"unchecked"})
public class EsServiceImpl<T> extends AbstractEsService<T> implements EsService<T> {

    private final Object LOCK = new Object();
    private final int DEFAULT_BATCH_SIZE = 1000;

    @Override
    public EsQueryWrapper<T> esQueryWrapper() {
        return new EsQueryWrapper<>(this.clazz);
    }

    @Override
    public EsChainQueryWrapper<T> esChainQueryWrapper() {
        return new EsChainQueryWrapper<>(this);
    }

    @Override
    public EsUpdateWrapper<T> esUpdateWrapper() {
        return new EsUpdateWrapper<>(this.clazz);
    }

    @Override
    public EsChainUpdateWrapper<T> esChainUpdateWrapper() {
        return new EsChainUpdateWrapper<>(this);
    }

    @Override
    public void createIndex() {
        esExecutor.createIndex(index, clazz);
    }

    @Override
    public void createIndexMapping() {
        esExecutor.createIndexMapping(index, clazz);
    }

    @Override
    public void createMapping() {
        esExecutor.createMapping(index, clazz);
    }

    @Override
    public boolean updateSettings(EsSettings esSettings) {
        return esExecutor.updateSettings(index, esSettings);
    }

    @Override
    public boolean save(T entity) {
        if (!super.exists) {
            synchronized (LOCK) {
                if (!super.exists) {
                    super.exists = true;
                    esExecutor.createIndexMapping(index, clazz);
                }
            }
        }
        return esExecutor.save(index, entity);
    }

    @Override
    public boolean saveOrUpdate(T entity) {
        if (!esExecutor.save(index, entity)) {
            return updateById(entity);
        }
        return true;
    }

    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList) {
        if (!exists) {
            synchronized (LOCK) {
                if (!super.exists) {
                    super.exists = true;
                    esExecutor.createIndexMapping(index, clazz);
                }
            }
        }
        return saveBatch(entityList, DEFAULT_BATCH_SIZE);
    }

    @Override
    public List<BulkItemResponse> saveBatch(Collection<T> entityList, int batchSize) {
        List<T> list = new ArrayList<>();
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        int i = 1;
        for (T t : entityList) {
            list.add(t);
            if (i % batchSize == 0) {
                esExecutor.saveBatch(index, list);
                list.clear();
            }
            i++;
        }
        esExecutor.saveBatch(index, list);
        return failBulkItemResponses;
    }


    /**
     * ?????? ID ??????
     *
     * @param id ??????ID
     */
    @Override
    public boolean removeById(Serializable id) {
        return esExecutor.delete(index, id.toString());
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        List<String> ids = idList.stream().map(Object::toString).collect(Collectors.toList());
        return esExecutor.deleteBatch(index, ids);
    }

    @Override
    public BulkByScrollResponse remove(EsWrapper<T> esUpdateWrapper) {
        return esExecutor.deleteByQuery(index, esUpdateWrapper.getQueryBuilder());
    }

    @Override
    public BulkByScrollResponse removeAll() {
        return esExecutor.deleteByQuery(index, QueryBuilders.matchAllQuery());
    }

    /**
     * ?????? ID ????????????
     *
     * @param entity ????????????
     */
    @Override
    public boolean updateById(T entity) {
        return esExecutor.update(index, entity);
    }

    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList) {
        return updateBatch(entityList, DEFAULT_BATCH_SIZE);
    }

    /**
     * ??????ID ????????????
     *
     * @param entityList ??????????????????
     * @param batchSize  ??????????????????
     */
    @Override
    public List<BulkItemResponse> updateBatch(Collection<T> entityList, int batchSize) {
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        List<T> list = new ArrayList<>();
        int i = 1;
        for (T t : entityList) {
            list.add(t);
            if (i % batchSize == 0) {
                failBulkItemResponses.addAll(doUpdateBatch(list));
                list.clear();
            }
            i++;
        }
        failBulkItemResponses.addAll(doUpdateBatch(list));
        return failBulkItemResponses;
    }

    private List<BulkItemResponse> doUpdateBatch(List<T> list) {
        return esExecutor.updateBatch(index, list);
    }

    @Override
    public BulkByScrollResponse updateByWrapper(EsWrapper<T> esUpdateWrapper) {
        return esExecutor.updateByWrapper(index, esUpdateWrapper);
    }

    @Override
    public void deleteIndex() {
        esExecutor.deleteIndex(index);
        super.exists = false;
    }

    /**
     * ?????? ID ??????
     *
     * @param id ??????ID
     */
    @Override
    public T getById(Serializable id) {
        List<String> ids = Collections.singletonList(id.toString());
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(ids);
        //??????
        EsResponse<T> esResponse = esExecutor.searchByWrapper(esQueryWrapper, clazz, index);
        List<T> list = esResponse.getList();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * ???????????????ID ???????????????
     *
     * @param idList ??????ID??????
     */
    @Override
    public List<T> listByIds(Collection<Serializable> idList) {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.ids(idList.stream().map(Objects::toString).collect(Collectors.toList()));
        //??????
        return esExecutor.searchByWrapper(esQueryWrapper, clazz, index).getList();
    }

    //????????????3??????
    @Override
    public EsResponse<T> list(EsQueryWrapper<T> esQueryWrapper) {
        //??????????????????
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return esExecutor.searchByWrapper(esQueryWrapper, clazz, index);
    }

    @Override
    public EsResponse<T> page(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return esExecutor.searchPageByWrapper(pageInfo, esQueryWrapper, clazz, index);
    }

    @Override
    public long count(EsQueryWrapper<T> esQueryWrapper) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }
        return esExecutor.count(esQueryWrapper, index);
    }

    @Override
    public EsAggregationsReponse<T> aggregations(EsQueryWrapper<T> esQueryWrapper) {
        return esExecutor.aggregations(index, esQueryWrapper);
    }


    @Override
    public void scroll(EsQueryWrapper<T> esQueryWrapper, int size, int keepTime, ScrollHandler<T> scrollHandler) {
        if (esQueryWrapper == null) {
            esQueryWrapper = matchAll();
        }

        esExecutor.scrollByWrapper(esQueryWrapper, clazz, index, size, keepTime, scrollHandler);
    }

    @Override
    public BulkByScrollResponse increment(EsWrapper<T> esUpdateWrapper) {
        return esExecutor.increment(index, esUpdateWrapper);
    }

    private EsQueryWrapper<T> matchAll() {
        EsQueryWrapper<T> esQueryWrapper = new EsQueryWrapper<>(clazz);
        esQueryWrapper.must().query(QueryBuilders.matchAllQuery());
        return esQueryWrapper;
    }

}
