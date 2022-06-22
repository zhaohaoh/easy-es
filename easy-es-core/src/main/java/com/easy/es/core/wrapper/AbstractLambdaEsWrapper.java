package com.easy.es.core.wrapper;

import com.easy.es.core.tools.LambdaUtils;
import com.easy.es.core.tools.SFunction;
import com.easy.es.exception.EsException;
import com.easy.es.core.tools.SerializedLambda;

import java.util.Arrays;
import java.util.Locale;

public abstract class AbstractLambdaEsWrapper<T, Children extends AbstractLambdaEsWrapper<T, Children>> extends AbstractEsWrapper<T, SFunction<T, ?>, Children> {

    @Override
    protected final String[] nameToString(SFunction<T, ?>... functions) {
        return Arrays.stream(functions).map(this::nameToString).toArray(String[]::new);
    }

    @Override
    protected String nameToString(SFunction<T, ?> function) {
        SerializedLambda lambda = LambdaUtils.resolve(function);
        return getColumn(lambda);
    }

    private String getColumn(SerializedLambda lambda) {
        return methodToProperty(lambda.getImplMethodName());
    }

    private String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new EsException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

}
