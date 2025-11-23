package com.itu.demo;


import java.lang.reflect.Method;

public class Mapping {
    private Class<?> controllerClass;
    private Method method;
    private URLPattern urlPattern;  // Nouveau: au lieu de String url

    public Mapping() {
    }
    public Mapping(Class<?> controllerClass, Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public Mapping(Class<?> controllerClass, Method method, String urlPattern) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.urlPattern = new URLPattern(urlPattern);
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

    @Override
    public String toString() {
        return "Mapping{" +
                "controllerClass=" + controllerClass.getName() +
                ", method=" + method.getName() +
                ", urlPattern=" + urlPattern +
                '}';
    }
}