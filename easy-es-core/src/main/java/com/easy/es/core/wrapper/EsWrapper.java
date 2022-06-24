package com.easy.es.core.wrapper;


import com.easy.es.pojo.EsHighLight;
import com.easy.es.pojo.EsOrder;
import com.easy.es.pojo.EsSelect;
import com.easy.es.pojo.EsUpdateField;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.List;

public interface EsWrapper<T> {
    List<EsOrder> getEsOrderList();

    EsSelect getEsSelect();

    EsSelect getSelect();

    EsHighLight getEsHighLight();

    BoolQueryBuilder getQueryBuilder();

    default EsUpdateField getEsUpdateField() {
        return null;
    }
}
