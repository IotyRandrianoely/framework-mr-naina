package com.itu.demo;

import mg.framework.annotations.HandleURL;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private List<Mapping> mappings;  // Changé de Map vers List pour permettre le matching
/* remettre en Mao comme dans sprint 3 */
    public Router() {
        this.mappings = new ArrayList<>();
    }

    public void addMapping(String urlPattern, Mapping mapping) {
        // Vérifier les conflits de pattern
        for (Mapping existingMapping : mappings) {
            if (existingMapping.getUrlPattern().getPattern().equals(urlPattern)) {
                throw new RuntimeException("URL pattern déjà mappé: " + urlPattern + 
                    ". Conflit entre " + existingMapping + " et " + mapping);
            }
        }
        mappings.add(mapping);
    }

    /**
     * Recherche un mapping qui correspond à l'URL donnée
     * Retourne le premier mapping dont le pattern match
     */
    public Mapping getMapping(String url) {
        for (Mapping mapping : mappings) {
            if (mapping.getUrlPattern().matches(url)) {
                return mapping;
            }
        }
        return null;
    }

    /**
     * Extrait les paramètres d'URL pour un mapping donné
     */
    public Map<String, String> extractParams(String url, Mapping mapping) {
        if (mapping != null) {
            return mapping.getUrlPattern().extractParams(url);
        }
        return new HashMap<>();
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void scanAndMap(String packageName) throws Exception {
        List<Class<?>> allClasses = Scanner.scanPackage(packageName);
        List<Class<?>> controllers = Scanner.getControllers(allClasses);
        
        for (Class<?> controller : controllers) {
            List<Method> methods = Scanner.getHandleURLMethods(controller);
            
            for (Method method : methods) {
                HandleURL annotation = method.getAnnotation(HandleURL.class);
                String urlPattern = annotation.value();
                
                if (urlPattern != null && !urlPattern.isEmpty()) {
                    Mapping mapping = new Mapping(controller, method, urlPattern);
                    addMapping(urlPattern, mapping);
                }
            }
        }
    }
}