package com.easy.es.core;

import com.easy.es.pojo.EsSettings;
import com.easy.es.properties.EsMappingParam;
import com.easy.es.properties.EsParamHolder;
import com.easy.es.annotation.EsField;
import com.easy.es.annotation.EsId;
import com.easy.es.annotation.EsIndex;
import com.easy.es.constant.EsFieldType;
import com.easy.es.exception.EsException;
import com.easy.es.properties.EsIndexParam;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.easy.es.util.ResolveUtils.*;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsAnnotationParamResolve {

    public EsIndexParam buildEsDocParam(Class<?> tClass) {
        EsIndex esIndex = tClass.getAnnotation(EsIndex.class);
        if (esIndex == null) {
            return null;
        }
        if (StringUtils.isBlank(esIndex.index())) {
            throw new EsException("es entity annotation @EsIndex no has index");
        }
        EsIndexParam esIndexParam = new EsIndexParam();
        esIndexParam.setIndex(esIndex.index());
        EsSettings esSettings = new EsSettings();
        esSettings.setNumberOfShards(esIndex.shard());
        esSettings.setNumberOfReplicas(esIndex.replices());
        esSettings.setRefreshInterval(esIndex.initRefreshInterval());
        esSettings.setMaxResultWindow(esIndex.initMaxResultWindow());
        if (StringUtils.isNotBlank(esIndex.defaultAnalyzer())) {
            esSettings.setDefaultAnalyzer(esIndex.defaultAnalyzer());
        }
        String[] analyzers = esIndex.analyzer();
        //添加自定义分词器
        if (ArrayUtils.isNotEmpty(analyzers)) {
            Map<String, Object> analysis = new HashMap<>();
            Map<String, Object> child = new HashMap<>();
            analysis.put("analyzer", child);
            for (String analyzerName : analyzers) {
                Map map = EsParamHolder.getAnalysis(analyzerName);
                if (map != null) {
                    child.put(analyzerName, map);
                }
            }
            esSettings.setAnalysis(analysis);
        }
        esIndexParam.setEsSettings(esSettings);
        return esIndexParam;
    }

    public List<EsMappingParam> buildMappingProperties(Class<?> tClass) {
        List<EsMappingParam> esMappingPropertiesList = new ArrayList<>();
        Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields) {
            EsId esId = field.getAnnotation(EsId.class);
            if (esId != null) {
                EsParamHolder.put(tClass, field);
            }

            field.setAccessible(true);
            EsMappingParam properties = new EsMappingParam();
            properties.setFieldName(field.getName());
            Class<?> type = field.getType();
            EsField esField = field.getAnnotation(EsField.class);
            if (esField != null && esField.exist()) {
                resolveEsField(properties, esField);
            }
            if (StringUtils.isBlank(properties.getType())) {
                //如果是map复杂类型直接设为object，通过新增的参数动态创建
                String fieldType = null;
                if (Map.class.isAssignableFrom(type)) {
                    fieldType = EsFieldType.OBJECT.name().toLowerCase();
                } else if (!isCommonDataType(type) && !isWrapClass(type) && !isDate(type)) {
                    if (Number.class.isAssignableFrom(type)) {
                        throw new EsException("Number类型无法自动解析");
                    }
                    //获取集合泛型类型
                    if (Collection.class.isAssignableFrom(type)) {
                        Type genericType = field.getGenericType();
                        ParameterizedType pt = (ParameterizedType) genericType;
                        Type typeArgument = pt.getActualTypeArguments()[0];
                        while (typeArgument instanceof ParameterizedType) {
                            typeArgument = ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                        }
                        type = (Class<?>) typeArgument;
                    } else {
                        //如果是对象类型
                        List<EsMappingParam> esMappingProperties = buildMappingProperties(type);
                        properties.setMappingProperties(esMappingProperties);
                        //如果不是嵌套类型
                        if ((esField == null) || (esField.type() != EsFieldType.NESTED)) {
                            fieldType = EsFieldType.OBJECT.name().toLowerCase();
                        }
                    }
                }
                if (fieldType == null) {
                    fieldType = setAutoFieldType(type);
                }
                properties.setType(isDate(type) ? EsFieldType.DATE.name().toLowerCase() : fieldType);
            }

            if (properties.getType().equalsIgnoreCase(EsFieldType.STRING.name())) {
                //双类型字符串的映射转换
                EsParamHolder.putTextKeyword(tClass, properties.getFieldName());
            }
            esMappingPropertiesList.add(properties);
        }
        return esMappingPropertiesList;
    }

    private void resolveEsField(EsMappingParam properties, EsField esField) {
        if (esField != null) {
            if (StringUtils.isNotBlank(esField.name())) {
                properties.setFieldName(esField.name());
            }
            if (esField.type() != null && esField.type() != EsFieldType.AUTO) {
                properties.setType(esField.type().name().toLowerCase());
            }
            if (StringUtils.isNotBlank(esField.analyzer())) {
                properties.setAnalyzer(esField.analyzer());
            }
            if (StringUtils.isNotBlank(esField.searchAnalyzer())) {
                properties.setSearchAnalyzer(esField.searchAnalyzer());
            }
            if (esField.type() != EsFieldType.NESTED && esField.store()) {
                properties.setStore(true);
            }
            if (esField.type() != EsFieldType.NESTED && !esField.index()) {
                properties.setIndex(false);
            }
        }
    }

    private String setAutoFieldType(Class<?> type) {
        String name = type.getName();
        String fieldType;
        if (isCommonDataType(type)) {
            fieldType = name.toLowerCase();
        } else {
            fieldType = StringUtils.substringAfterLast(name, ".").toLowerCase();
        }
        if ("int".equalsIgnoreCase(name)) {
            fieldType = EsFieldType.INTEGER.name();
        }
        return fieldType.toLowerCase();
    }
}
