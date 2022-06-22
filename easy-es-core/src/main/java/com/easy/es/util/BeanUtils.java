package com.easy.es.util;

import org.springframework.cglib.beans.BeanMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtils {
    public static <T> Map<String, ?> beanToMap(T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        Map<String, Object> map = new HashMap<>();

        beanMap.forEach((key, value) -> {
            map.put(String.valueOf(key), value);
        });
        return map;
    }

    public static <T> T mapToBean(Map<String, ?> map, Class<T> clazz) {
        T bean = null;
        try {
            bean = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("instance error mapToBean");
        }
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }

    public static <T> List<Map<String, ?>> beansToMaps(List<T> beans) {
        List<Map<String, ?>> list = new ArrayList<>();
        if (beans != null && beans.size() > 0) {
            Map<String, ?> map = null;
            T bean = null;
            for (int i = 0, size = beans.size(); i < size; i++) {
                bean = beans.get(i);
                map = beanToMap(bean);
                list.add(map);
            }
        }
        return list;
    }

    public static <T> List<T> mapsToBeans(List<Map<String, ?>> maps, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (maps != null && maps.size() > 0) {
            Map<String, ?> map = null;
            for (int i = 0, size = maps.size(); i < size; i++) {
                map = maps.get(i);
                T bean = mapToBean(map, clazz);
                list.add(bean);
            }
        }
        return list;
    }
}
