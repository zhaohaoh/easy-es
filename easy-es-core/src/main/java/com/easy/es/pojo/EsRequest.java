package com.easy.es.pojo;

import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hzh
 * @Date: 2021/9/17 16:49
 * es当中的数据  如果存对象Object或者map  默认会创建  实体类字段名.子字段名.keyword和体类字段名.子字段名.text
 * 如果存数组或者list   默认都会存成es中的数组字段  如 "list": [ "测试","开发"]
 * 那么就可以使用多词条（terms）查询类型，查询测试和开发的文档   一个term(keyword不分词精准匹配) 代表只要数组中有一个则查询出。  terms是or  有一个就查出
 * 2个term则必须满足两个term的值才查询出
 * terms查询
 * {
 * "query": {
 * "terms": {
 * "list.keyword":  ["测试","开发"]
 * }
 * }
 * }
 * 布尔查询多个term and
 * {"query":{"bool":{"must":[{"term":{"list.keyword":"开发"}},{"term":{"list.keyword":"测试"}}],"must_not":[],"should":[]}}}
 */

@Data
@Deprecated
public class EsRequest {
    //索性
    private String index;
    //默认查所有文档 需要自定义用这个替换 BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();  boolQuery是组合查询
    //match是全文检索（分词检索）
    private BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

    private Integer page = 1;

    private Integer size = 10;

    private Boolean isPage = true;
    //高亮字段
    private EsHighLight esHighLight;
    //查询结果包含字段
    private String[] includes;
    //查询结果不包含字段
    private String[] excludes;
    //精确聚合字段
    private List<EsTermGroupField> termsGroupFields;
    //过滤聚合字段
    private List<EsFilterGroupField> filterGroupFields;
    //范围聚合字段
    private List<EsRangeGroupField> rangeGroupFields;
    //用来手动构造聚合参数
    private AggregationBuilder aggregationBuilder;
    //排序字段
    private List<EsOrder> orderFields = new ArrayList<>();
}
