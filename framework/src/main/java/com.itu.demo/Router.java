package com.itu.demo;

import mg.framework.annotations.HandleURL;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Router {
    private Map<String, Mapping> urlMappings;

    public Router() {
        // LinkedHashMap pour préserver l'ordre d'insertion (priorité de matching)
        this.urlMappings = new LinkedHashMap<>();
    }

    public void addMapping(String url, Mapping mapping) {
        if (urlMappings.containsKey(url)) {
            throw new RuntimeException("URL déjà mappée: " + url + 
                ". Conflit entre " + urlMappings.get(url) + " et " + mapping);
        }
        urlMappings.put(url, mapping);
    }

    /**
     * Recherche un mapping :
     * 1) tentative d'égalité exacte
     * 2) parcours des mappings et test du pattern via URLPattern.matches(...)
     */
    public Mapping getMapping(String url) {
        // 1) match exact
        Mapping exact = urlMappings.get(url);
        if (exact != null) return exact;

        // 2) match par pattern
        for (Entry<String, Mapping> e : urlMappings.entrySet()) {
            Mapping m = e.getValue();
            if (m != null && m.getUrlPattern() != null && m.getUrlPattern().matches(url)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Retourne les mappings (pratique pour affichage / debug dans FrontServlet)
     */
    public List<Mapping> getMappings() {
        return new ArrayList<>(urlMappings.values());
    }

    /**
     * Expose la Map si besoin
     */
    public Map<String, Mapping> getUrlMappings() {
        return urlMappings;
    }

    /**
     * Extrait les paramètres d'URL pour un mapping donné (ex: id=1 pour /etudiant/{id})
     */
    public Map<String, String> extractParams(String url, Mapping mapping) {
        if (mapping != null && mapping.getUrlPattern() != null) {
            return mapping.getUrlPattern().extractParams(url);
        }
        return new HashMap<>();
    }

    public void scanAndMap(String packageName) throws Exception {
        List<Class<?>> allClasses = Scanner.scanPackage(packageName);
        List<Class<?>> controllers = Scanner.getControllers(allClasses);
        
        for (Class<?> controller : controllers) {
            List<Method> methods = Scanner.getHandleURLMethods(controller);
            
            for (Method method : methods) {
                HandleURL annotation = method.getAnnotation(HandleURL.class);
                String url = annotation.value();
                
                if (url != null && !url.isEmpty()) {
                    // Construire le Mapping en incluant le pattern pour pouvoir matcher dynamiquement
                    Mapping mapping = new Mapping(controller, method, url);
                    addMapping(url, mapping);
                }
            }
        }
    }
}