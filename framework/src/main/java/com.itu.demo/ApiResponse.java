package com.itu.demo;

import java.util.HashMap;
import java.util.Map;

/**
 * Sprint 9: Classe pour structurer les réponses API REST
 */
public class ApiResponse {
    private String status;  // "success" ou "error"
    private Object data;    // Les données de la réponse
    private String message; // Message optionnel
    
    public ApiResponse() {
    }
    
    public ApiResponse(String status, Object data) {
        this.status = status;
        this.data = data;
    }
    
    public ApiResponse(String status, Object data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
    
    /**
     * Crée une réponse de succès
     */
    public static ApiResponse success(Object data) {
        return new ApiResponse("success", data);
    }
    
    /**
     * Crée une réponse de succès avec message
     */
    public static ApiResponse success(Object data, String message) {
        return new ApiResponse("success", data, message);
    }
    
    /**
     * Crée une réponse d'erreur
     */
    public static ApiResponse error(String message) {
        return new ApiResponse("error", null, message);
    }
    
    /**
     * Crée une réponse d'erreur avec données
     */
    public static ApiResponse error(Object data, String message) {
        return new ApiResponse("error", data, message);
    }

    // Getters et Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
