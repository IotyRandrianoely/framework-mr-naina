package com.itu.demo;

import mg.framework.annotations.HandleURL;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private Map<String, Mapping> urlMappings;

    public Router() {
        this.urlMappings = new HashMap<>();
    }

    public void addMapping(String url, Mapping mapping) {
        if (urlMappings.containsKey(url)) {
            throw new RuntimeException("URL déjà mappée: " + url + 
                ". Conflit entre " + urlMappings.get(url) + " et " + mapping);
        }
        urlMappings.put(url, mapping);
    }

    public Mapping getMapping(String url) {
        return urlMappings.get(url);
    }

    public Map<String, Mapping> getUrlMappings() {
        return urlMappings;
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
                    Mapping mapping = new Mapping(controller, method);
                    addMapping(url, mapping);
                }
            }
        }
    }
}