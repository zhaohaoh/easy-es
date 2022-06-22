package com.easy.es.properties;

import com.easy.es.core.EsAnnotationParamResolve;
import com.easy.es.exception.EsException;
import com.easy.es.util.XcontentBuildUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: hzh
 * @Date: 2022/1/24 15:27
 */
public class EsParamHolder {
    private static final Logger logger = LoggerFactory.getLogger(EsParamHolder.class);
    // 属性解析器
    private static final EsAnnotationParamResolve ES_ANNOTATION_PARAM_RESOLVE = new EsAnnotationParamResolve();
    // id的map
    private static final Map<String, String> ID_MAP = new ConcurrentHashMap<>();
    // 索引map
    private static final Map<String, String> INDEX_MAP = new ConcurrentHashMap<>();
    // 属性映射的map
    private static final Map<String, List<EsMappingParam>> ESMAPPINGPARAM_MAP = new ConcurrentHashMap<>();
    // 转换keyword
    private static final Map<String, Map<String, String>> CONVERT_KEYWORD_MAP = new ConcurrentHashMap<>();
    // 分词器
    private static final Map<String, Map> ANALYSIS_MAP = new ConcurrentHashMap<>();

    static {
        Map<String, Object> map = XcontentBuildUtils.buildAnalyzer("custom", new String[]{"stemmer", "lowercase", "asciifolding"}, "standard");
        Map<String, Object> simple = XcontentBuildUtils.buildAnalyzer("simple", new String[]{"stemmer", "lowercase", "asciifolding"}, "simple");
        Map<String, Object> customIk = XcontentBuildUtils.buildAnalyzer("custom", new String[]{"stemmer", "unique", "asciifolding"}, "ik_max_word");
        EsParamHolder.putAnalysis("custom", map);
        EsParamHolder.putAnalysis("simple", simple);
        EsParamHolder.putAnalysis("customIk", customIk);
    }

    public static <T> String getDocId(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        try {
            String idFeildName = ID_MAP.get(clazz.getName());
            Field field;
            if (idFeildName != null) {
                field = clazz.getDeclaredField(idFeildName);
            } else {
                field = clazz.getDeclaredField("id");
                logger.info("elasticsearch autoGetDocId  name=id");
            }
            field.setAccessible(true);
            Long id = (Long) field.get(obj);
            if (id == null) {
                throw new EsException("elasticsearch doc id not found");
            }
            return String.valueOf(id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("es getId error ", e);
        }
        throw new EsException("elasticsearch doc id not found");
    }

    public static void put(Class<?> clazz, Field field) {
        ID_MAP.put(clazz.getName(), field.getName());
    }

    public static String getIndex(Class<?> clazz) {
        return INDEX_MAP.get(clazz.getName());
    }

    public static String putIndex(Class<?> clazz, String index) {
        INDEX_MAP.put(clazz.getName(), index);
        return index;
    }

    public static void removeIndex(String index) {
        INDEX_MAP.entrySet().removeIf(entry -> entry.getValue().equals(index));
    }

    public static List<EsMappingParam> getMappingProperties(Class<?> clazz) {
        return ESMAPPINGPARAM_MAP.computeIfAbsent(clazz.getName(), params -> ES_ANNOTATION_PARAM_RESOLVE.buildMappingProperties(clazz));
    }

    public static EsIndexParam getEsIndexParam(Class<?> clazz) {
        return ES_ANNOTATION_PARAM_RESOLVE.buildEsDocParam(clazz);
    }

    public static String getStringKeyword(Class<?> clazz, String name) {
        Map<String, String> map = CONVERT_KEYWORD_MAP.get(clazz.getName());
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        return map.get(name);
    }

    public static void putTextKeyword(Class<?> clazz, String name) {
        Map<String, String> map = CONVERT_KEYWORD_MAP.computeIfAbsent(clazz.getName(), p -> new HashMap<>());
        map.put(name, name + ".keyword");
    }

    public static Map getAnalysis(String name) {
        return ANALYSIS_MAP.get(name);
    }

    public static void putAnalysis(String name, Map map) {
        ANALYSIS_MAP.put(name, map);
    }

}
