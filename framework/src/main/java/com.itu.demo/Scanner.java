package com.itu.demo;

import mg.framework.annotations.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Scanner {
    
    public static List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        
        if (resource == null) {
            throw new Exception("Package non trouve: " + packageName);
        }
        
        File directory = new File(resource.toURI());
        if (directory.exists()) {
            scanDirectory(directory, packageName, classes);
        }
        
        return classes;
    }
    
    private static void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public static List<Class<?>> getControllers(List<Class<?>> classes) {
        List<Class<?>> controllers = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                controllers.add(clazz);
            }
        }
        return controllers;
    }
    
    /**
     * Retourne toutes les méthodes annotées avec @HandleURL, @GetMapping, @PostMapping, 
     * @PutMapping, @DeleteMapping ou @RequestMapping
     */
    public static List<Method> getHandleURLMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(HandleURL.class) ||
                method.isAnnotationPresent(GetMapping.class) ||
                method.isAnnotationPresent(PostMapping.class) ||
                method.isAnnotationPresent(PutMapping.class) ||
                method.isAnnotationPresent(DeleteMapping.class) ||
                method.isAnnotationPresent(RequestMapping.class)) {
                methods.add(method);
            }
        }
        return methods;
    }
    
    /**
     * Extrait l'URL et les méthodes HTTP d'une méthode annotée
     */
    public static MappingInfo extractMappingInfo(Method method, Class<?> controllerClass) {
        String url = null;
        List<String> httpMethods = new ArrayList<>();
        
        // Récupérer le préfixe du controller si @RequestMapping est présent sur la classe
        String controllerPrefix = "";
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
            controllerPrefix = classMapping.value();
        }
        
        // Vérifier @GetMapping
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            url = controllerPrefix + annotation.value();
            httpMethods.add("GET");
        }
        // Vérifier @PostMapping
        else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            url = controllerPrefix + annotation.value();
            httpMethods.add("POST");
        }
        // Vérifier @PutMapping
        else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping annotation = method.getAnnotation(PutMapping.class);
            url = controllerPrefix + annotation.value();
            httpMethods.add("PUT");
        }
        // Vérifier @DeleteMapping
        else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
            url = controllerPrefix + annotation.value();
            httpMethods.add("DELETE");
        }
        // Vérifier @RequestMapping
        else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            url = controllerPrefix + annotation.value();
            String[] methods = annotation.method();
            if (methods.length > 0) {
                for (String m : methods) {
                    httpMethods.add(m.toUpperCase());
                }
            }
        }
        // Vérifier @HandleURL (rétrocompatibilité)
        else if (method.isAnnotationPresent(HandleURL.class)) {
            HandleURL annotation = method.getAnnotation(HandleURL.class);
            url = controllerPrefix + annotation.value();
            // HandleURL accepte toutes les méthodes HTTP
        }
        
        return new MappingInfo(url, httpMethods);
    }
    
    /**
     * Classe interne pour retourner les informations de mapping
     */
    public static class MappingInfo {
        private String url;
        private List<String> httpMethods;
        
        public MappingInfo(String url, List<String> httpMethods) {
            this.url = url;
            this.httpMethods = httpMethods;
        }
        
        public String getUrl() {
            return url;
        }
        
        public List<String> getHttpMethods() {
            return httpMethods;
        }
    }
}