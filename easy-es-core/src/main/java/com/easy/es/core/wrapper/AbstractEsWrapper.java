package com.easy.es.core.wrapper;


import com.easy.es.core.tools.SFunction;
import com.easy.es.core.wrapper.aggregation.EsAggregationWrapper;
import com.easy.es.pojo.EsHighLight;
import com.easy.es.pojo.EsOrder;
import com.easy.es.pojo.EsSelect;
import com.easy.es.properties.EsParamHolder;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractEsWrapper<T, R extends SFunction<T, ?>, Children extends AbstractEsWrapper<T, R, Children>> extends AbstractLambdaEsWrapper<T, R>
        implements IEsQueryWrapper<Children, R>, EsWrapper<T> {
    protected AbstractEsWrapper() {
    }

    protected List<EsOrder> esOrderList;

    @Override
    public List<EsOrder> getEsOrderList() {
        return esOrderList;
    }

    protected Class<T> tClass;

    protected Children children = (Children) this;

    protected BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

    protected MatchQueryBuilder temp;

    protected abstract Children instance();

    protected EsHighLight esHighLight;

    protected EsAggregationWrapper<T> esAggregationWrapper;

    private List<QueryBuilder> queryBuilders = queryBuilder.must();
    //查询结果包含字段
    private EsSelect esSelect;


    public void setEsAggregationWrapper(EsAggregationWrapper<T> esAggregationWrapper) {
        this.esAggregationWrapper = esAggregationWrapper;
        esAggregationWrapper.setClass(tClass);
    }

    public EsAggregationWrapper<T> getEsAggregationWrapper() {
        if (esAggregationWrapper == null) {
            esAggregationWrapper = new EsAggregationWrapper<>(tClass);
        }
        return esAggregationWrapper;
    }

    @Override
    protected String nameToString(R function) {
        return super.nameToString(function);
    }


    @Override
    public EsSelect getEsSelect() {
        return esSelect;
    }

    public void setEsSelect(EsSelect esSelect) {
        this.esSelect = esSelect;
    }

    @Override
    public EsHighLight getEsHighLight() {
        return esHighLight;
    }


    @Override
    public BoolQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    //获取select的字段
    @Override
    public EsSelect getSelect() {
        EsSelect esSelect = this.getEsSelect();
        if (esSelect == null) {
            this.setEsSelect(new EsSelect());
        }
        return this.getEsSelect();
    }

    public void matchAll() {
        queryBuilder.must(QueryBuilders.matchAllQuery());
    }

    public Children boost(float boost) {
        queryBuilder.boost(boost);
        return this.children;
    }

    public Children must(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.must(children.queryBuilder);
        return this.children;
    }

    public Children should(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.should(children.queryBuilder);
        return this.children;
    }

    public Children mustNot(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.mustNot(children.queryBuilder);
        return this.children;
    }

    public Children filters(Consumer<Children> consumer) {
        final Children children = instance();
        consumer.accept(children);
        this.children.queryBuilder.filter(children.queryBuilder);
        return this.children;
    }

    public Children must() {
        if (queryBuilders != queryBuilder.must()) {
            queryBuilders = queryBuilder.must();
        }
        return children;
    }

    public Children should() {
        if (queryBuilders != queryBuilder.should()) {
            queryBuilders = queryBuilder.should();
        }
        return children;
    }

    public Children filter() {
        if (queryBuilders != queryBuilder.filter()) {
            queryBuilders = queryBuilder.filter();
        }
        return children;
    }

    public Children mustNot() {
        if (queryBuilders != queryBuilder.mustNot()) {
            queryBuilders = queryBuilder.mustNot();
        }
        return children;
    }

    //match方法中配合or使用，百分比匹配
    public void minimumShouldMatch(String minimumShouldMatch) {
        temp.minimumShouldMatch(minimumShouldMatch);
    }

    //配合or(should)使用，表示有几个符合的
    public void minimumShouldMatch(Integer minimumShouldMatch) {
        queryBuilder.minimumShouldMatch(minimumShouldMatch);
    }

    @Override
    public Children query(boolean condition, QueryBuilder queryBuilder) {
        if (condition) {
            queryBuilders.add(queryBuilder);
        }
        return children;
    }

    @Override
    public Children exists(boolean condition, R name) {
        if (condition) {
            queryBuilders.add(QueryBuilders.existsQuery(nameToString(name)));
        }
        return children;
    }

    @Override
    public Children term(boolean condition, R name, Object value) {
        if (condition) {
            String keyword = nameToString(name);
            if (tClass != null) {
                //获取需要加.keyword的字段
                String key = EsParamHolder.getStringKeyword(tClass, keyword);
                if (StringUtils.isNotBlank(key)) {
                    keyword = key;
                }
            }
            queryBuilders.add(QueryBuilders.termQuery(keyword, value));
        }
        return children;
    }

    @Override
    public Children terms(boolean condition, R name, Object... value) {
        if (condition) {
            String keyword = nameToString(name);
            if (tClass != null) {
                //获取需要加.keyword的字段
                String key = EsParamHolder.getStringKeyword(tClass, keyword);
                if (StringUtils.isNotBlank(key)) {
                    keyword = key;
                }
            }
            queryBuilders.add(QueryBuilders.termsQuery(keyword, value));
        }
        return children;
    }

    @Override
    public Children terms(boolean condition, R name, Collection<?> values) {
        if (condition) {
            String keyword = nameToString(name);
            if (tClass != null) {
                //获取需要加.keyword的字段
                String key = EsParamHolder.getStringKeyword(tClass, keyword);
                if (StringUtils.isNotBlank(key)) {
                    keyword = key;
                }
            }
            queryBuilders.add(QueryBuilders.termsQuery(keyword, values));
        }
        return children;
    }


    @Override
    public Children termKeyword(boolean condition, R name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.termQuery(nameToString(name) + ".keyword", value));
        }
        return children;
    }


    @Override
    public Children termsKeyword(boolean condition, R name, Object... values) {
        if (condition) {
            queryBuilders.add(QueryBuilders.termsQuery(nameToString(name) + ".keyword", values));
        }
        return children;
    }

    @Override
    public Children termsKeyword(boolean condition, R name, Collection<?> values) {
        if (condition) {
            queryBuilders.add(QueryBuilders.termsQuery(nameToString(name) + ".keyword", values));
        }
        return children;
    }

    @Override
    public Children match(boolean condition, R name, Object value) {
        if (condition) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery(nameToString(name), value);
            temp = matchQuery;
            queryBuilders.add(matchQuery);
        }
        return children;
    }

    @Override
    public Children matchPhrase(boolean condition, R name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.matchPhraseQuery(nameToString(name), value));
        }
        return children;
    }

    @Override
    public Children multiMatch(boolean condition, Object value, R... name) {
        if (condition) {
            queryBuilders.add(QueryBuilders.multiMatchQuery(value, nameToString(name)));
        }
        return children;
    }

    @Override
    public Children matchPhrasePrefix(boolean condition, R name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.matchPhrasePrefixQuery(nameToString(name), value));
        }
        return children;
    }

    @Override
    public Children wildcard(boolean condition, R name, String value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.wildcardQuery(nameToString(name), value));
        }
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, R name, String value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.fuzzyQuery(nameToString(name), value));
        }
        return children;
    }

    //TODO 迟点用 根据id查询
    @Override
    public Children ids(boolean condition, Collection<String> ids) {
        if (condition) {
            queryBuilders.add(QueryBuilders.idsQuery().addIds(ids.toArray(new String[ids.size()])));
        }
        return children;
    }

    @Override
    public Children gt(boolean condition, R name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).gt(from));
        }
        return children;
    }

    @Override
    public Children ge(boolean condition, R name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).gte(from));
        }
        return children;
    }

    @Override
    public Children lt(boolean condition, R name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).lt(to));
        }
        return children;
    }

    @Override
    public Children le(boolean condition, R name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).lte(to));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, R name, Object from, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).from(from, true).to(to, true));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, R name, Object from, Object to, boolean include) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(nameToString(name)).from(from, include).to(to, include));
        }
        return children;
    }


    /**
     * -----------下面的根据name查询，这里违反了设计原则但是方便了
     */

    @Override
    public Children exists(boolean condition, String name) {
        if (condition) {
            queryBuilders.add(QueryBuilders.existsQuery(name));
        }
        return children;
    }

    @Override
    public Children term(boolean condition, String name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.termQuery(name, value));
        }
        return this.children;
    }

    @Override
    public Children terms(boolean condition, String name, Object... value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.termsQuery(name, value));
        }
        return children;
    }

    @Override
    public Children terms(boolean condition, String name, Collection<Object> values) {
        if (condition) {
            queryBuilders.add(QueryBuilders.termsQuery(name, values));
        }
        return children;
    }

    @Override
    public Children match(boolean condition, String name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.matchQuery(name, value));
        }
        return children;
    }

    @Override
    public Children matchPhrase(boolean condition, String name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.matchPhraseQuery(name, value));
        }
        return children;
    }

    @Override
    public Children multiMatch(boolean condition, Object value, String... name) {
        if (condition) {
            queryBuilders.add(QueryBuilders.multiMatchQuery(value, name));
        }
        return children;
    }

    @Override
    public Children matchPhrasePrefix(boolean condition, String name, Object value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.matchPhrasePrefixQuery(name, value));
        }
        return children;
    }

    @Override
    public Children wildcard(boolean condition, String name, String value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.wildcardQuery(name, value));
        }
        return children;
    }

    //有纠错能力的模糊查询。
    @Override
    public Children fuzzy(boolean condition, String name, String value) {
        if (condition) {
            queryBuilders.add(QueryBuilders.fuzzyQuery(name, value));
        }
        return children;
    }


    @Override
    public Children gt(boolean condition, String name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).gt(from));
        }
        return children;
    }

    @Override
    public Children ge(boolean condition, String name, Object from) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).gte(from));
        }
        return children;
    }

    @Override
    public Children lt(boolean condition, String name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).lt(to));
        }
        return children;
    }

    @Override
    public Children le(boolean condition, String name, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).lte(to));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, String name, Object from, Object to) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).from(from, true).to(to, true));
        }
        return children;
    }

    @Override
    public Children between(boolean condition, String name, Object from, Object to, boolean include) {
        if (condition) {
            queryBuilders.add(QueryBuilders.rangeQuery(name).from(from, include).to(to, include));
        }
        return children;
    }

    public Children includes(R... func) {
        String[] includes = nameToString(func);
        EsSelect esSelect = getSelect();
        esSelect.setIncludes(includes);
        return (Children) this;
    }

    public Children includes(String... names) {
        EsSelect esSelect = getSelect();
        esSelect.setIncludes(names);
        return (Children) this;
    }

    public Children excludes(R... func) {
        String[] includes = nameToString(func);
        EsSelect esSelect = getSelect();
        esSelect.setExcludes(includes);
        return (Children) this;
    }

    public Children excludes(String... names) {
        EsSelect esSelect = getSelect();
        esSelect.setExcludes(names);
        return (Children) this;
    }


    public Children orderBy(String order, R... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        String[] arr = nameToString(columns);
        for (String name : arr) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(order);
            esOrderList.add(esOrder);
        }
        return children;
    }


    public Children orderBy(String order, String... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(order);
            esOrderList.add(esOrder);
        }
        return children;
    }


    public Children orderByAsc(String... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.ASC.name());
            esOrderList.add(esOrder);
        }
        return children;
    }


    public Children orderByDesc(String... columns) {
        if (esOrderList == null) {
            esOrderList = new ArrayList<>();
        }
        for (String name : columns) {
            EsOrder esOrder = new EsOrder();
            esOrder.setName(name);
            esOrder.setSort(SortOrder.DESC.name());
            esOrderList.add(esOrder);
        }
        return children;
    }

}
