package com.easy.es.core.service;


import com.easy.es.annotation.EsId;
import com.easy.es.annotation.EsIndex;
import com.easy.es.core.EsExecutor;
import com.easy.es.properties.EsParamHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:11
 */
public abstract class AbstractEsService<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEsService.class);
    protected boolean exists;
    protected String type;
    protected String index;
    protected Class<T> clazz;
    @Autowired
    protected EsExecutor esExecutor;
    @Value("${es_suffix:}")
    private String esSuffix;

    public String getEsSuffix() {
        return "_" + esSuffix;
    }


    @PostConstruct
    @SuppressWarnings({"unchecked"})
    public void init() {
        try {
            Type tClazz = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            clazz = (Class<T>) tClazz;
            EsIndex annotation = clazz.getAnnotation(EsIndex.class);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(EsId.class) != null) {
                    EsParamHolder.put(clazz, field);
                }
            }
            index = EsParamHolder.putIndex(clazz, annotation.index() +  getEsSuffix());
            type = annotation.type();
            exists = esExecutor.indexExists(index);
            EsParamHolder.getMappingProperties(clazz);
            logger.warn("init es index={} exists={}", index, exists);
        } catch (Exception e) {
            logger.error("init es service index exception", e);
        }
    }

}
