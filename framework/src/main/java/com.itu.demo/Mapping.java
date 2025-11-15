package com.itu.demo;


import java.lang.reflect.Method;

public class Mapping {
    private Class<?> controllerClass;
    private Method method;

    public Mapping() {
    }

    public Mapping(Class<?> controllerClass, Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
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

    // Nouvelle méthode pour vérifier si la méthode retourne un String
    public boolean returnsString() {
        return method.getReturnType().equals(String.class);
    }

    // Nouvelle méthode pour invoquer la méthode
    public String invoke() throws Exception {
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
        Object result = method.invoke(controllerInstance);
        return (String) result;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "controllerClass=" + controllerClass.getName() +
                ", method=" + method.getName() +
                ", returnType=" + method.getReturnType().getSimpleName() +
                '}';
    }
}