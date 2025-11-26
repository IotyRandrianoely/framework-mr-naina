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

    // Vérifier si la méthode retourne un String
    public boolean returnsString() {
        return method.getReturnType().equals(String.class);
    }

    // Nouvelle méthode pour vérifier si la méthode retourne un ModelView
    public boolean returnsModelView() {
        return method.getReturnType().equals(ModelView.class);
    }

    // Invoquer la méthode et retourner le résultat
    public Object invokeMethod() throws Exception {
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
        return method.invoke(controllerInstance);
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