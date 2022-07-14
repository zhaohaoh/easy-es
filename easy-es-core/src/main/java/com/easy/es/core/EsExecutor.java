package com.easy.es.core;


import com.easy.es.constant.EsFieldType;
import com.easy.es.core.wrapper.EsQueryWrapper;
import com.easy.es.core.wrapper.EsWrapper;
import com.easy.es.core.wrapper.aggregation.EsAggregationWrapper;
import com.easy.es.exception.EsException;
import com.easy.es.pojo.*;
import com.easy.es.properties.EsIndexParam;
import com.easy.es.properties.EsMappingParam;
import com.easy.es.properties.EsParamHolder;
import com.easy.es.util.BeanUtils;
import com.easy.es.util.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.easy.es.constant.EsConstant.*;
import static com.easy.es.util.ResolveUtils.isCommonDataType;
import static com.easy.es.util.ResolveUtils.isWrapClass;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
@Service
public class EsExecutor {
    private static final Logger log = LoggerFactory.getLogger(EsExecutor.class);
    public static final String ES_TYPE = "_doc";
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private BulkProcessor bulkProcessor;

    public void createIndex(String index, Class<?> tClass) {
        if (StringUtils.isBlank(index)) {
            throw new EsException("createMapping index not exists");
        }
        EsIndexParam esDocParam = EsParamHolder.getEsIndexParam(tClass);
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        indexRequest(esDocParam, indexRequest);
    }

    public void createMapping(String index, Class<?> tClass) {
        mappingRequest(index, tClass);
    }

    public void createIndexMapping(String index, Class<?> tClass) {
        EsIndexParam esDocParam = EsParamHolder.getEsIndexParam(tClass);
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        indexMappingRequest(tClass, esDocParam, indexRequest);
    }

    public void asyncSaveBatch(String index, List<?> objects) {
        for (Object esData : objects) {
            IndexRequest indexRequest = new IndexRequest(index).type(ES_TYPE).id(EsParamHolder.getDocId(esData)).source(JsonUtils.toJsonStr(esData), XContentType.JSON);
            bulkProcessor.add(indexRequest);
        }
    }

    public List<BulkItemResponse> saveBatch(String index, List<?> esDataList) {
        BulkRequest bulkRequest = new BulkRequest();
        List<BulkItemResponse> failBulkItemResponses = new ArrayList<>();
        List<IndexRequest> requests = new ArrayList<>();
        for (Object esData : esDataList) {
            IndexRequest saveRequest = new IndexRequest(index);
            saveRequest.id(EsParamHolder.getDocId(esData)).source(JsonUtils.toJsonStr(esData), XContentType.JSON);
            requests.add(saveRequest);
        }
        for (IndexRequest request : requests) {
            bulkRequest.add(request);
        }
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        BulkResponse res = null;
        try {
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("es saveBatch index={}", index);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    log.error("save error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                    failBulkItemResponses.add(bulkItemResponse);
                }
            }
        } catch (IOException e) {
            throw new EsException("SaveBatch IOException", e);
        }
        return failBulkItemResponses;
    }


    public boolean save(String index, Object esData) {
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.id(EsParamHolder.getDocId(esData)).source(JsonUtils.toJsonStr(esData), XContentType.JSON);
        bulkRequest.add(indexRequest);
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        BulkResponse res = null;
        try {
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                DocWriteResponse response = bulkItemResponse.getResponse();
                IndexResponse indexResponse = (IndexResponse) response;
                if (bulkItemResponse.isFailed()) {
                    throw new EsException("es save error" + bulkItemResponse.getFailureMessage());
                } else {
                    log.info("es save success index={} data={}", index, JsonUtils.toJsonStr(esData));
                }
            }
        } catch (IOException e) {
            throw new EsException("Save IOException", e);
        }
        return true;
    }

    /**
     * 更新Es数据
     *
     * @param esData Es数据对象
     * @return
     * @throws Exception
     */
    public boolean update(String index, Object esData) {
        UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
        //乐观锁重试次数
        updateRequest.retryOnConflict(5);
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        try {
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                log.error("es update index={} data={}  error reason: doc  deleted", index, JsonUtils.toJsonStr(esData));
                return false;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                //noop标识没有数据改变。前后的值相同
                return false;
            } else {
                log.info("es update success index={} data={}", index, JsonUtils.toJsonStr(esData));
            }
        } catch (IOException e) {
            throw new EsException("elasticsearch update io error", e);
        } catch (ElasticsearchException e) {
            //版本冲突
            if (e.status() == RestStatus.CONFLICT) {
                throw new EsException("elasticsearch update error  version conflict");
            }
            //找不到
            if (e.status() == RestStatus.NOT_FOUND) {
                log.error("es update index={} data={}  error reason:  not found doc", index, JsonUtils.toJsonStr(esData));
                return false;
            }
        } catch (Exception e) {
            throw new EsException("elasticsearch update error", e);
        }
        return true;
    }


    public List<BulkItemResponse> updateBatch(String index, List<?> esDataList) {
        BulkRequest bulkRequest = new BulkRequest();
        List<UpdateRequest> requests = new ArrayList<>();
        for (Object esData : esDataList) {
            UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
            updateRequest.retryOnConflict(5);
            requests.add(updateRequest);
        }
        for (UpdateRequest request : requests) {
            bulkRequest.add(request);
        }
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        BulkResponse res = null;
        List<BulkItemResponse> responses = new ArrayList<>();
        try {
            res = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            boolean hasFailures = res.hasFailures();
            log.info("es updateBatch index={} data:{} hasFailures={}", index, JsonUtils.toJsonStr(esDataList), hasFailures);
            for (BulkItemResponse bulkItemResponse : res.getItems()) {
                if (bulkItemResponse.isFailed()) {
                    responses.add(bulkItemResponse);
                    log.error("updateBatch error" + bulkItemResponse.getId() + " message:" + bulkItemResponse.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new EsException("updateBatch IOException", e);
        }
        return responses;
    }


    public <T> BulkByScrollResponse updateByWrapper(String index, EsWrapper<T> esUpdateWrapper) {
        List<EsUpdateField.Field> fields = esUpdateWrapper.getEsUpdateField().getFields();
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        StringBuilder script = new StringBuilder();
        for (EsUpdateField.Field field : fields) {
            String name = field.getName();
            //除了基本类型和字符串。日期的对象需要进行转化
            Object value = field.getValue();
            if (value instanceof Date) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = (Date) value;
                value = simpleDateFormat.format(date);
            } else if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (value instanceof LocalDate) {
                value = ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (value instanceof List) {
            } else if (!isCommonDataType(value.getClass()) && !isWrapClass(value.getClass())) {
                value = BeanUtils.beanToMap(value);
            }
            //list直接覆盖 丢进去 无需再特殊处理
            params.put(name, value);
            script.append("ctx._source.");
            script.append(name).append(" = params.").append(name).append(";");
        }
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        //版本号不匹配更新失败不停止
        request.setConflicts("proceed");
        request.setQuery(esUpdateWrapper.getQueryBuilder());
        request.setBatchSize(1000);
        request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        Script painless = new Script(ScriptType.INLINE, "painless", script.toString(), params);
        request.setScript(painless);
        try {
            log.info("updateByWrapper requst: script:{},params={}", script, params);
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            log.info("updateByWrapper response:{} update count=", bulkResponse);
            return bulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper IOException", e);
        }
    }

    public <T> BulkByScrollResponse increment(String index, EsWrapper<T> esUpdateWrapper) {
        List<EsUpdateField.Field> fields = esUpdateWrapper.getEsUpdateField().getIncrementFields();
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        StringBuilder script = new StringBuilder();
        for (EsUpdateField.Field field : fields) {
            String name = field.getName();
            Long value = (Long) field.getValue();
            params.put(name, value);
            script.append("ctx._source.");
            script.append(name).append(" += params.").append(name).append(";");
        }
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        //版本号不匹配更新失败不停止
        request.setConflicts("proceed");
        request.setQuery(esUpdateWrapper.getQueryBuilder());
        // 一次批处理的大小.因为是滚动处理的
        request.setBatchSize(1000);
        request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        Script painless = new Script(ScriptType.INLINE, "painless", script.toString(), params);
        request.setScript(painless);
        try {
            log.info("updateByWrapper increment requst: script:{},params={}", script, params);
            BulkByScrollResponse bulkResponse =
                    restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
            log.info("updateByWrapper increment response:{} update count=", bulkResponse);
            return bulkResponse;
        } catch (IOException e) {
            throw new EsException("updateByWrapper increment IOException", e);
        }
    }


    /**
     * 批量更新Es数据
     *
     * @param esDataList Es数据列表
     * @throws Exception
     */
    public void asyncUpdateBatch(String index, List<?> esDataList) {
//        BulkRequest bulkRequest = new BulkRequest();
//        esDataList.forEach(esData -> {
//            UpdateRequest updateRequest = new UpdateRequest(index, "1").doc(JsonUtils.toJsonStr(esDataList), XContentType.JSON);
//            bulkRequest.add(updateRequest);
//        });
        for (Object esData : esDataList) {
            UpdateRequest updateRequest = new UpdateRequest(index, EsParamHolder.getDocId(esData)).doc(JsonUtils.toJsonStr(esData), XContentType.JSON);
            bulkProcessor.add(updateRequest);
        }
    }

    public boolean delete(String index, String id) {
        DeleteRequest deleteRequest = new DeleteRequest(index, id);
        try {
            deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.info("es delete index={}", index);
        } catch (IOException e) {
            throw new EsException("es delete error", e);
        }
        return true;
    }

    public BulkByScrollResponse deleteByQuery(String index, QueryBuilder queryBuilder) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(queryBuilder);
        // 更新最大文档数
        request.setMaxDocs(10000);
        request.setMaxRetries(10);
        request.setBatchSize(10000);
        // 刷新索引
        request.setRefresh(true);
        // 使用滚动参数来控制“搜索上下文”存活的时间
        request.setScroll(TimeValue.timeValueMinutes(10));
        // 超时
        request.setTimeout(TimeValue.timeValueMinutes(2));
        // 更新时版本冲突
        request.setConflicts("proceed");
        try {
            SearchSourceBuilder source = request.getSearchRequest().source();
            log.info("elasticsearch delete body:" + source.toString());
            BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            return bulkByScrollResponse;
        } catch (Exception e) {
            throw new EsException("es delete error", e);
        }
    }

    /**
     * 删除所有
     */
    public void deleteAll(String index) {
        if (index.endsWith("_pro")) {
            throw new EsException("禁止删除");
        }
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setMaxRetries(10);
        request.setQuery(new MatchAllQueryBuilder());
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            log.info("Es deleteAll index={}", index);
        } catch (IOException e) {
            throw new EsException("Es delete error", e);
        }
    }

    public boolean deleteBatch(String index, Collection<String> esDataList) {
        log.info("Es deleteBatch index={} ids={}", index, esDataList);
        BulkRequest bulkRequest = new BulkRequest();
        esDataList.forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, id);
            bulkRequest.add(deleteRequest);
        });
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            BulkItemResponse[] items = bulkResponse.getItems();
            for (BulkItemResponse item : items) {
                if (item.isFailed()) {
                    log.error("es deleteBatch index={} id={} FailureMessage=:{}", index, item.getId(), item.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new EsException("Es delete error", e);
        }
        return true;
    }


    /**
     * 删除索引
     */
    public void deleteIndex(String index) {
        EsParamHolder.removeIndex(index);
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        try {
            restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("deleteIndex index={}", index);
        } catch (IOException e) {
            throw new RuntimeException("delete index error ", e);
        }
    }

    /**
     * 查询index是否存在
     */
    public boolean indexExists(String index) {
        try {
            return restHighLevelClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("index exists", e);
        }
    }

    //统计
    public <T> long count(EsQueryWrapper<T> esQueryWrapper, String index) {
        CountRequest countRequest = new CountRequest();
        countRequest.query(esQueryWrapper.getQueryBuilder());
        countRequest.indices(index);
        CountResponse count = null;
        try {
            log.info("elasticsearch count index=:{} body:{}", index, esQueryWrapper.getQueryBuilder().toString());
            count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("es count error ", e);
        }
        if (count != null) {
            return count.getCount();
        }
        return 0;
    }

    public <T> EsResponse<T> searchByWrapper(EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index) {
        return search(null, esQueryWrapper, tClass, index);
    }

    public <T> EsResponse<T> searchPageByWrapper(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index) {
        return search(pageInfo, esQueryWrapper, tClass, index);
    }

    public <T> void scrollByWrapper(EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index, int size, int keepTime, ScrollHandler<T> scrollHandler) {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(keepTime));
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(esQueryWrapper.getQueryBuilder());
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            //调用scroll处理
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            List<T> result = new ArrayList<>();
            while (searchHits != null && searchHits.length > 0) {
                for (SearchHit searchHit : searchHits) {
                    T t = JsonUtils.toBean(searchHit.getSourceAsString(), tClass);
                    result.add(t);
                }
                scrollHandler.handler(result);
                result.clear();
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean succeeded = clearScrollResponse.isSucceeded();
        } catch (Exception e) {
            throw new EsException("scroll error", e);
        }
    }

    // 聚合
    public <T> EsAggregationsReponse<T> aggregations(String index, EsQueryWrapper<T> esQueryWrapper) {
        SearchRequest searchRequest = new SearchRequest();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.size(0);
        populateGroupField(esQueryWrapper.getEsAggregationWrapper(), sourceBuilder);
        //设置索引
        searchRequest.source(sourceBuilder);
        searchRequest.indices(index);
        //查询
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            log.info("elasticsearch aggregations index={} body:{}", index, sourceBuilder);
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long end = System.currentTimeMillis();
            log.info("elasticsearch aggregations Time={}", end - start);
        } catch (Exception e) {
            log.error("es aggregations error", e);
            throw new EsException("elasticsearch aggregations error");
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("elasticsearch aggregations error");
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsAggregationsReponse<T> esAggregationReponse = new EsAggregationsReponse<>();
        esAggregationReponse.setAggregations(aggregations);
        esAggregationReponse.settClass(esQueryWrapper.gettClass());
        return esAggregationReponse;
    }

    public <T> EsResponse<T> search(PageInfo<T> pageInfo, EsQueryWrapper<T> esQueryWrapper, Class<T> tClass, String index) {
        SearchRequest searchRequest = new SearchRequest();
        //查询条件组合
        BoolQueryBuilder queryBuilder = esQueryWrapper.getQueryBuilder();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        EsSelect esSelect = esQueryWrapper.getEsSelect();
        if (esSelect != null) {
            if (ArrayUtils.isNotEmpty(esSelect.getIncludes()) || ArrayUtils.isNotEmpty(esSelect.getExcludes())) {
                sourceBuilder.fetchSource(esSelect.getIncludes(), esSelect.getExcludes());
            }
        }
        sourceBuilder.size(10000);
        //是否需要分页查询
        if (pageInfo != null) {
            //设置分页属性
            sourceBuilder.from((int) ((pageInfo.getPage() - 1) * pageInfo.getSize()));
            sourceBuilder.size((int) pageInfo.getSize());
        }
        //设置高亮
        if (esQueryWrapper.getEsHighLight() != null) {
            EsHighLight esHighLight = esQueryWrapper.getEsHighLight();
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //设置为0获取全部内容
            highlightBuilder.numOfFragments(0);
            //高亮字段
            esHighLight.getFields().forEach(highlightBuilder::field);
            //高亮前后缀
            highlightBuilder.preTags(esHighLight.getPreTag())
                    .postTags(esHighLight.getPostTag())
                    .fragmentSize(esHighLight.getFragmentSize());
            sourceBuilder.highlighter(highlightBuilder);
        }
        //超过1万条加了才能返回
        sourceBuilder.trackTotalHits(true);

        //排序
        if (!CollectionUtils.isEmpty(esQueryWrapper.getEsOrderList())) {
            List<EsOrder> orderFields = esQueryWrapper.getEsOrderList();
            orderFields.forEach(order -> {
                sourceBuilder.sort(new FieldSortBuilder(order.getName()).order(SortOrder.valueOf(order.getSort())));
            });
        }
//        else {
//            sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
//        }
        populateGroupField(esQueryWrapper.getEsAggregationWrapper(), sourceBuilder);
        //设置索引
        searchRequest.source(sourceBuilder);
        searchRequest.indices(index);
        //查询
        SearchResponse searchResponse = null;
        try {
            long start = System.currentTimeMillis();
            log.info("elasticsearch search index={} body:{}", index, sourceBuilder);
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long end = System.currentTimeMillis();
            log.info("elasticsearch search Time={}", end - start);
        } catch (Exception e) {
            log.error("es查询失败", e);
            throw new EsException("elasticsearch search error");
        }
        if (searchResponse.status().getStatus() != 200) {
            throw new EsException("elasticsearch search error");
        }
        //获取结果集
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitArray = hits.getHits();
        List<T> result = new ArrayList<>();
        if (esQueryWrapper.getEsHighLight() != null) {
            for (SearchHit hit : hitArray) {
                //获取高亮字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                //将Json数据转化为实体对象
                Map<String, Object> map = hit.getSourceAsMap();
                if (highlightFields != null) {
                    highlightFields.forEach((k, v) -> {
                                Text[] texts = v.fragments();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (Text text : texts) {
                                    stringBuilder.append(text);
                                }
                                //高亮字段重新put进去
                                map.put(k, stringBuilder.toString());
                            }
                    );

                }
                T t = BeanUtils.mapToBean(map, tClass);
                result.add(t);
            }
        } else {
            for (SearchHit hit : hitArray) {
                result.add(JsonUtils.toBean(hit.getSourceAsString(), tClass));
            }
        }
        Aggregations aggregations = searchResponse.getAggregations();
        EsAggregationsReponse<T> esAggregationsReponse = new EsAggregationsReponse<>();
        esAggregationsReponse.setAggregations(aggregations);
        esAggregationsReponse.settClass(esQueryWrapper.gettClass());
        return new EsResponse<T>(result, hits.getTotalHits().value, esAggregationsReponse);
    }

    //填充分组字段
    private void populateGroupField(EsAggregationWrapper<?> esAggregationWrapper, SearchSourceBuilder sourceBuilder) {
        if (esAggregationWrapper.getAggregationBuilder() != null) {
            for (BaseAggregationBuilder aggregation : esAggregationWrapper.getAggregationBuilder()) {
                if (aggregation instanceof AggregationBuilder) {
                    sourceBuilder.aggregation((AggregationBuilder) aggregation);
                } else {
                    sourceBuilder.aggregation((PipelineAggregationBuilder) aggregation);
                }
            }
        }
    }

    public boolean updateSettings(String index, EsSettings esSettings) {
        String json = JsonUtils.toJsonStr(esSettings);
        Settings settings = Settings.builder().loadFromSource(json, XContentType.JSON).build();
        //创建索引的settings
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(settings, index);

        //执行put
        AcknowledgedResponse settingsResult = null;
        try {
            settingsResult = restHighLevelClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //成功的话，返回结果是true
        return settingsResult.isAcknowledged();
    }

    private void indexRequest(EsIndexParam esIndexParam, CreateIndexRequest indexRequest) {
        //创建索引的settings
        Settings.Builder settings = Settings.builder();
        if (esIndexParam != null) {
            EsSettings esSettings = esIndexParam.getEsSettings();
            if (esSettings != null) {
                String json = JsonUtils.toJsonStr(esSettings);
                settings.loadFromSource(json, XContentType.JSON);
            }
        }
        try {
            //将settings封装到一个IndexClient对象中
            indexRequest
                    .settings(settings);
            CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void indexMappingRequest(Class<?> tClass, EsIndexParam esIndexParam, CreateIndexRequest indexRequest) {
        List<EsMappingParam> esMappingProperties = EsParamHolder.getMappingProperties(tClass);
        //创建索引的settings
        Settings.Builder settings = Settings.builder();
        if (esIndexParam != null) {
            EsSettings esSettings = esIndexParam.getEsSettings();
            if (esSettings != null) {
                String json = JsonUtils.toJsonStr(esSettings);
                settings.loadFromSource(json, XContentType.JSON);
            }
        }
        try {
            XContentBuilder xContentBuilder;
            xContentBuilder = XContentFactory.jsonBuilder().startObject();
            xContentBuilder = populateXcontentBuilder(esMappingProperties, xContentBuilder);
            xContentBuilder.endObject();

            boolean exists = this.indexExists(indexRequest.index());
            if (!exists) {
                indexRequest
                        .settings(settings)
                        .mapping(xContentBuilder);
                log.info("ES createMapping info={}", xContentBuilder.getOutputStream().toString());
                CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
                if (esIndexParam != null) {
                    EsParamHolder.putIndex(tClass, indexRequest.index());
                }
            }
        } catch (IOException e) {
            throw new EsException("elasticsearch mappingRequest error", e);
        }
    }

    private void mappingRequest(String index, Class<?> tClass) {
        List<EsMappingParam> esMappingProperties = EsParamHolder.getMappingProperties(tClass);

        try {
            XContentBuilder xContentBuilder;
            xContentBuilder = XContentFactory.jsonBuilder().startObject();
            xContentBuilder = populateXcontentBuilder(esMappingProperties, xContentBuilder);
            xContentBuilder.endObject();
            //将settings和mappings封装到一个IndexClient对象中
            PutMappingRequest putMappingRequest = new PutMappingRequest(index);
//            putMappingRequest.source(BytesReference.bytes(xContentBuilder), xContentBuilder.contentType());
            putMappingRequest.source(xContentBuilder);
            log.info("ES createMapping info={}", xContentBuilder.getOutputStream().toString());
            restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("elasticsearch mappingRequest error", e);
        }
    }

    private XContentBuilder populateXcontentBuilder(List<EsMappingParam> esMappingProperties, XContentBuilder xContentBuilder) throws IOException {
        xContentBuilder
                .startObject("properties");
        for (EsMappingParam esMappingProperty : esMappingProperties) {
            xContentBuilder.startObject(esMappingProperty.getFieldName());
            if (!CollectionUtils.isEmpty(esMappingProperty.getMappingProperties())) {
                xContentBuilder = populateXcontentBuilder(esMappingProperty.getMappingProperties(), xContentBuilder);
            } else {
                if (StringUtils.isBlank(esMappingProperty.getType())) {
                    throw new EsException("es创建映射类型为空");
                }
                //如果是字符串创建text并且在后面创建keyword
                if (EsFieldType.STRING.name().equalsIgnoreCase(esMappingProperty.getType())) {
                    xContentBuilder.field(TYPE, "text");
                } else {
                    // 否则正常设置类型 包括嵌套对象
                    xContentBuilder.field(TYPE, esMappingProperty.getType());
                    if (Objects.equals(esMappingProperty.getType(), EsFieldType.DATE.name().toLowerCase())) {
                        xContentBuilder.field("format", "yyyy-MM-dd HH:mm:ss");
                    }
                }
                if (StringUtils.isNotBlank(esMappingProperty.getAnalyzer())) {
                    xContentBuilder.field(ANALYZER, esMappingProperty.getAnalyzer());
                }
                if (StringUtils.isNotBlank(esMappingProperty.getSearchAnalyzer())) {
                    xContentBuilder.field(SEARCH_ANALYZER, esMappingProperty.getSearchAnalyzer());
                }
                if (esMappingProperty.getStore() != null) {
                    xContentBuilder.field(STORE, esMappingProperty.getStore());
                }
                if (esMappingProperty.getIndex() != null) {
                    xContentBuilder.field(INDEX, esMappingProperty.getIndex());
                }
                //是字符串类型同时创建keyword
                if (EsFieldType.STRING.name().equalsIgnoreCase(esMappingProperty.getType())) {
                    xContentBuilder.startObject(FIELDS).startObject(EsFieldType.KEYWORD.name().toLowerCase())
                            .field(IGNORE_ABOVE, 256).field(TYPE, EsFieldType.KEYWORD.name().toLowerCase())
                            .endObject().endObject();
                }
            }
            //endFieldName
            xContentBuilder.endObject();
        }
        //endProperties
        xContentBuilder.endObject();
        return xContentBuilder;
    }

}
