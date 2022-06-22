package com.easy.es.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EsHighLight {
    /**
     * 高亮前缀
     */
    private String preTag;

    /**
     * 高亮后缀
     */
    private String postTag;

    /**
     * 高亮字段列表
     */
    private List<String> fields;

    private Integer fragmentSize = 2000;

    /**
     * 构造高亮对象
     */
    public EsHighLight(String... field) {
        this.fields = new ArrayList<>(2);
        this.fields.addAll(Arrays.asList(field));
        this.preTag = "<em color=\"red\">";
        this.postTag = "</em>";
    }

    public EsHighLight(String preTag, String postTag,  String... field) {
        this.fields = new ArrayList<>(2);
        this.fields.addAll(Arrays.asList(field));
        this.preTag = preTag;
        this.postTag = postTag;
    }

    public EsHighLight addField(String field) {
        if (this.fields == null) {
            this.fields = new ArrayList<>(2);
        }
        this.fields.add(field);
        return this;
    }

    public String getPreTag() {
        return preTag;
    }

    public void setPreTag(String preTag) {
        this.preTag = preTag;
    }

    public String getPostTag() {
        return postTag;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Integer getFragmentSize() {
        return fragmentSize;
    }

    public void setFragmentSize(Integer fragmentSize) {
        this.fragmentSize = fragmentSize;
    }

    @Override
    public String toString() {
        return "EsHighLight{" +
                "preTag='" + preTag + '\'' +
                ", postTag='" + postTag + '\'' +
                ", fields=" + fields +
                ", fragmentSize=" + fragmentSize +
                '}';
    }
}
