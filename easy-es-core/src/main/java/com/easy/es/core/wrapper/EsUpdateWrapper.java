package com.easy.es.core.wrapper;


import com.easy.es.core.tools.SFunction;
import com.easy.es.pojo.EsUpdateField;

import java.util.List;

/**
 * @Author: hzh
 * @Date: 2022/1/21 11:10
 */
public class EsUpdateWrapper<T> extends AbstractEsWrapper<T, SFunction<T,?>, EsUpdateWrapper<T>>  implements  Update<EsUpdateWrapper<T>, SFunction<T, ?>> {
    private EsUpdateField esUpdateField = new EsUpdateField();

    public EsUpdateWrapper<T> set(String name, Object value) {
        List<EsUpdateField.Field> fields = esUpdateField.getFields();
        EsUpdateField.Field field = new EsUpdateField.Field(name, value);
        fields.add(field);
        return this;
    }
    public EsUpdateWrapper(Class<T> tClass) {
        super.tClass = tClass;
    }

    public EsUpdateWrapper() {
    }

    @Override
    protected EsUpdateWrapper<T> instance() {
        return new EsUpdateWrapper<T>(super.tClass);
    }

    @Override
    public EsUpdateField getEsUpdateField() {
        return esUpdateField;
    }

    @Override
    public EsUpdateWrapper<T> set(SFunction<T, ?> column, Object val) {
        return set(nameToString(column), val);
    }

    @Override
    public EsUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            set(nameToString(column), val);
        }
        return this;
    }
}
