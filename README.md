# easy-es

##  框架实现elasticsearch简单封装

## 简单案例

### 服务类继承

![image-20220124165157541](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220124165157541.png)

### 实体类

@EsId必须标注es文档id  不标注默认取id 建议加上

![image-20220124165820266](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220124165820266.png)

如果只需要KEYWORD类型必须加上注解。影响term查询

@Esfield非必填。不填自动映射类型。除了NESTED  其他特殊的复杂类型暂不支持

### 手动创建映射

![image-20220124165305737](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220124165305737.png)

### 查询

#### 不建议的方式

不建议直接无参构造new对象。会导致term lamda表达式查询的问题。除非都用字符串匹配name.

建议有参构造

![image-20220125173219733](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220125173219733.png)

#### 根据id查询

![image-20220125174157338](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220125174157338.png)

#### 普通查询

![image-20220125172959455](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220125172959455.png)

#### 链式调用

![image-20220125174537789](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220125174537789.png)

```
EsQueryWrapper<SysUser> esQueryWrapper = new EsQueryWrapper<>(SysUser.class);
TermsAggregationBuilder username = AggregationBuilders.terms("user").field("email.keyword").order(BucketOrder.count(true));
EsAggregationWrapper<SysUser> esAggregationWrapper = esQueryWrapper.getEsAggregationWrapper();
DateHistogramAggregationBuilder date = esAggregationWrapper.dateHistogram(SysUser::getId).fixedInterval(DateHistogramInterval.days(10000));
date.subAggregation(esAggregationWrapper.sum(SysUser::getSex));
MaxAggregationBuilder max = esAggregationWrapper.max(SysUser::getSex);
esAggregationWrapper.add(date).add(max);
esAggregationWrapper.add(esAggregationWrapper.avgBucket(SysUser::getSex, "id_date_histogram>sex_sum"));
esQueryWrapper.setEsAggregationWrapper(esAggregationWrapper);
EsAggregationsReponse<SysUser> aggregations = sysUserEsService.aggregations(esQueryWrapper);
Max m = aggregations.getMax(SysUser::getSex);
Terms terms1 = aggregations.getTerms(SysUser::getUsername);
for (Terms.Bucket bucket : terms1.getBuckets()) {
    Aggregations aggregations1 = bucket.getAggregations();
}
```
