package com.easy.es.pojo;

import java.util.List;

public class EsGroupField {
    List<EsFilterGroupField> esFilterGroupFields;
    List<EsRangeGroupField> esRangeGroupFields;
    List<EsTermGroupField> esTermGroupFields;

    public List<EsFilterGroupField> getEsFilterGroupFields() {
        return esFilterGroupFields;
    }

    public void setEsFilterGroupFields(List<EsFilterGroupField> esFilterGroupFields) {
        this.esFilterGroupFields = esFilterGroupFields;
    }

    public List<EsRangeGroupField> getEsRangeGroupFields() {
        return esRangeGroupFields;
    }

    public void setEsRangeGroupFields(List<EsRangeGroupField> esRangeGroupFields) {
        this.esRangeGroupFields = esRangeGroupFields;
    }

    public List<EsTermGroupField> getEsTermGroupFields() {
        return esTermGroupFields;
    }

    public void setEsTermGroupFields(List<EsTermGroupField> esTermGroupFields) {
        this.esTermGroupFields = esTermGroupFields;
    }

    @Override
    public String toString() {
        return "EsGroupField{" +
                "esFilterGroupFields=" + esFilterGroupFields +
                ", esRangeGroupFields=" + esRangeGroupFields +
                ", esTermGroupFields=" + esTermGroupFields +
                '}';
    }
}
