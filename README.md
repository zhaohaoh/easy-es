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



#### 聚合查询

```java
EsQueryWrapper<SysUser> esQueryWrapper = new EsQueryWrapper<>(SysUser.class);

EsAggregationWrapper<SysUser>esAggregationWrapper=esQueryWrapper.getEsAggregationWrapper();

DateHistogramAggregationBuilder dateAggBuilder = esAggregationWrapper
    .dateHistogram(SysUser::getDate)
	.fixedInterval(DateHistogramInterval.days(100));

dateAggBuilder.subAggregation(esAggregationWrapper.sum(SysUser::getNum));

MaxAggregationBuilder max = esAggregationWrapper.max(SysUser::getAge);

esAggregationWrapper.add(dateAggBuilder)
    .add(max)
    .add(esAggregationWrapper.avgBucket(SysUser::getNum, "date_date_histogram>num_sum"));
```
