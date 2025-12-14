package com.itu.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Sprint 9: Utilitaire pour convertir des objets en JSON
 */
public class JsonUtil {
    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    
    /**
     * Convertit un objet en JSON
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    /**
     * Convertit une chaîne JSON en objet
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
