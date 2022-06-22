package com.easy.es.core.chain;

import com.easy.es.core.ScrollHandler;
import com.easy.es.core.tools.SFunction;
import com.easy.es.pojo.EsResponse;
import com.easy.es.pojo.PageInfo;
import com.easy.es.core.wrapper.EsQueryWrapper;
import com.easy.es.core.service.EsService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EsChainQueryWrapper<T> extends AbstractEsChainWrapper<T, SFunction<T, ?>, EsChainQueryWrapper<T>, EsQueryWrapper<T>> {
    private final EsService<T> esService;

    public EsChainQueryWrapper(EsService<T> esService) {
        this.esService = esService;
        Type tClazz = ((ParameterizedType) esService.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        super.tClass = (Class<T>) tClazz;
        super.esWrapper = new EsQueryWrapper<>(tClass);
    }

    public EsResponse<T> list() {
        return esService.list(super.esWrapper);
    }

    public EsResponse<T> page(long page, long size) {
        return esService.page(new PageInfo<>(page, size), super.esWrapper);
    }

    public long count() {
        return esService.count(super.esWrapper);
    }

    public void scroll(int size, ScrollHandler<T> scrollHandler) {
        esService.scroll(super.esWrapper, size, 1, scrollHandler);
    }

}
