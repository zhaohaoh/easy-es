package com.easy.es.properties;


import com.easy.es.pojo.EsSettings;

public class EsIndexParam {

    private String index;
    private EsSettings esSettings;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public EsSettings getEsSettings() {
        return esSettings;
    }

    public void setEsSettings(EsSettings esSettings) {
        this.esSettings = esSettings;
    }
}
