package com.itu.demo;
import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String view;
    private Map<String, Object> data;
    
    public ModelView() {
        this.data = new HashMap<>();
    }

    public ModelView(String view) {
        this();
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
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    /**
     * Ajoute un objet dans la map de données
     */
    public void addObject(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        this.data.put(key, value);
    }
    
    /**
     * Sprint 8: Alias de addObject pour correspondre à la convention
     */
    public void addData(String key, Object value) {
        this.addObject(key, value);
    }
    
    /**
     * Ajoute plusieurs objets à la fois
     */
    public void addAllData(Map<String, Object> data) {
        if (data != null) {
            this.data.putAll(data);
        }
    }
    
    @Override
    public String toString() {
        return "ModelView{view='" + view + "', data=" + data + "}";
    }
}