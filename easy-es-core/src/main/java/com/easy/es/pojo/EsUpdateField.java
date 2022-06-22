package com.easy.es.pojo;


import java.util.ArrayList;
import java.util.List;

public class EsUpdateField {
    List<Field> fields = new ArrayList<>();

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public static class Field {
        private String name;
        private Object value;

        public Field(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return "EsUpdateField{" +
                "fields=" + fields +
                '}';
    }
}
