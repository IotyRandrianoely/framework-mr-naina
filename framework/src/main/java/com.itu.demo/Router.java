package com.itu.demo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Router {
    private Map<String, List<Mapping>> urlMappings;  // Modifié: List<Mapping> pour supporter plusieurs méthodes HTTP

    public Router() {
        this.urlMappings = new LinkedHashMap<>();
    }

    public void addMapping(String url, Mapping mapping) {
        if (!urlMappings.containsKey(url)) {
            urlMappings.put(url, new ArrayList<>());
        }
        
        // Vérifier les conflits de méthodes HTTP
        List<Mapping> existingMappings = urlMappings.get(url);
        for (Mapping existing : existingMappings) {
            for (String httpMethod : mapping.getHttpMethods()) {
                if (existing.supportsHttpMethod(httpMethod)) {
                    throw new RuntimeException(
                        "Conflit de mapping pour URL: " + url + " avec méthode HTTP: " + httpMethod +
                        ". Conflit entre " + existing + " et " + mapping
                    );
                }
            }
        }
        
        existingMappings.add(mapping);
    }

    /**
     * Recherche un mapping en fonction de l'URL ET de la méthode HTTP
     */
    public Mapping getMapping(String url, String httpMethod) {
        // 1) Match exact
        List<Mapping> exactMappings = urlMappings.get(url);
        if (exactMappings != null) {
            for (Mapping mapping : exactMappings) {
                if (mapping.supportsHttpMethod(httpMethod)) {
                    return mapping;
                }
            }
        }

        // 2) Match par pattern
        for (Entry<String, List<Mapping>> entry : urlMappings.entrySet()) {
            for (Mapping mapping : entry.getValue()) {
                if (mapping.getUrlPattern() != null && 
                    mapping.getUrlPattern().matches(url) && 
                    mapping.supportsHttpMethod(httpMethod)) {
                    return mapping;
                }
            }
        }
        
        return null;
    }

    /**
     * Retourne tous les mappings (pour debug/affichage)
     */
    public List<Mapping> getMappings() {
        List<Mapping> allMappings = new ArrayList<>();
        for (List<Mapping> mappings : urlMappings.values()) {
            allMappings.addAll(mappings);
        }
        return allMappings;
    }

    /**
     * Extrait les paramètres d'URL pour un mapping donné
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
                Scanner.MappingInfo info = Scanner.extractMappingInfo(method, controller);
                String url = info.getUrl();
                List<String> httpMethods = info.getHttpMethods();
                
                if (url != null && !url.isEmpty()) {
                    Mapping mapping;
                    if (httpMethods.isEmpty()) {
                        // Pas de méthode HTTP spécifiée, accepte tout
                        mapping = new Mapping(controller, method, url);
                    } else {
                        // Méthodes HTTP spécifiques
                        mapping = new Mapping(controller, method, url, 
                                            httpMethods.toArray(new String[0]));
                    }
                    addMapping(url, mapping);
                }
            }
        }
    }
}