package com.itu.demo;
import java.util.HashMap;
import java.util.Map;
public class ModelView {
    private String view;
    private Map<String, Object> data;
    public ModelView() {
    }

    public ModelView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
    public Map<String, Object> getData() {
        return data;
    }   
    /*
    @param key
    @param value
    */
    public void addObject(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        this.data.put(key, value);
    }
    @Override
    public String toString() {
        return "ModelView{" +
                "view='" + view + '\'' +
                '}';
    }
}