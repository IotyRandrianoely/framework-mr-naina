package com.itu.demo;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Mapping {
    private Class<?> controllerClass;
    private Method method;
    private URLPattern urlPattern;
    private Set<String> httpMethods;  // Nouveau: GET, POST, PUT, DELETE, etc.

    public Mapping() {
        this.httpMethods = new HashSet<>();
    }
    public Mapping(Class<?> controllerClass, Method method) {
        this();
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public Mapping(Class<?> controllerClass, Method method, String urlPattern) {
        this();
        this.controllerClass = controllerClass;
        this.method = method;
        this.urlPattern = new URLPattern(urlPattern);
    }

    public Mapping(Class<?> controllerClass, Method method, String urlPattern, String... httpMethods) {
        this(controllerClass, method, urlPattern);
        if (httpMethods != null && httpMethods.length > 0) {
            this.httpMethods.addAll(Arrays.asList(httpMethods));
        }
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public URLPattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(URLPattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(Set<String> httpMethods) {
        this.httpMethods = httpMethods;
    }

    public void addHttpMethod(String method) {
        this.httpMethods.add(method.toUpperCase());
    }

    public boolean supportsHttpMethod(String method) {
        // Si aucune méthode HTTP n'est spécifiée, on accepte toutes les méthodes
        if (httpMethods == null || httpMethods.isEmpty()) {
            return true;
        }
        return httpMethods.contains(method.toUpperCase());
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "controllerClass=" + controllerClass.getName() +
                ", method=" + method.getName() +
                ", urlPattern=" + urlPattern +
                ", httpMethods=" + httpMethods +
                '}';
    }
}