package com.itu.demo;

import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.util.Map;
import java.util.List;
import java.lang.reflect.Method;

@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {
    
    private Router router;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        router = new Router();
        
        String packageToScan = config.getInitParameter("controllerPackage");
        if (packageToScan == null || packageToScan.isEmpty()) {
            packageToScan = "controller";
        }
        
        try {
            router.scanAndMap(packageToScan);
            System.out.println("===== URL Mappings =====");
            for (Mapping mapping : router.getMappings()) {
                System.out.println("Pattern: " + mapping.getUrlPattern().getPattern() + 
                                 " [" + mapping.getHttpMethods() + "] -> " + 
                                 mapping.getControllerClass().getSimpleName() + "." + 
                                 mapping.getMethod().getName() + "()");
            }
            System.out.println("========================");
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controllers", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Définir l'encodage UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String resourcePath = requestURI.substring(contextPath.length());
        String httpMethod = request.getMethod();
        
        try {
            java.net.URL resource = getServletContext().getResource(resourcePath);
            if (resource != null && !resourcePath.equals("/")) {
                RequestDispatcher defaultServlet = getServletContext().getNamedDispatcher("default");
                if (defaultServlet != null) {
                    defaultServlet.forward(request, response);
                    return;
                }
            }
        } catch (Exception e) {
            // Continuer le traitement
        }
        
        Mapping mapping = router.getMapping(resourcePath, httpMethod);
        
        if (mapping != null) {
            // Extraire les paramètres d'URL (path params: /products/{id})
            Map<String, String> pathParams = router.extractParams(resourcePath, mapping);
            
            // Sprint 8: Créer une Map<String, Object> avec TOUS les paramètres
            java.util.Map<String, Object> allParams = new java.util.HashMap<>();
            
            // Ajouter les path params
            allParams.putAll(pathParams);
            
            // Ajouter les query params et form params depuis request.getParameterMap()
            Map<String, String[]> parameterMap = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                
                // Si un seul paramètre, on stocke la String directement
                // Sinon on stocke le tableau String[]
                if (values != null && values.length > 0) {
                    if (values.length == 1) {
                        allParams.put(key, values[0]);
                    } else {
                        allParams.put(key, values);
                    }
                }
            }
            
            try {
                // Sprint 8: Invoquer avec injection automatique de Map<String, Object>
                Object result = invokeMethodWithParams(mapping, allParams);
                
                // Traiter le résultat
                if (result instanceof ModelView) {
                    ModelView mv = (ModelView) result;
                    
                    // Sprint 8: Copier mv.getData() dans les attributs de requête
                    if (mv.getData() != null) {
                        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                    
                    // Ajouter les urlParams pour accès dans JSP
                    request.setAttribute("urlParams", pathParams);
                    
                    // Forward vers la vue
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/" + mv.getView());
                    dispatcher.forward(request, response);
                    return;
                    
                } else if (result instanceof String) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println(result);
                    return;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException("Erreur lors de l'invocation: " + e.getMessage(), e);
            }
            
            // Fallback
            request.setAttribute("mapping", mapping);
            request.setAttribute("url", resourcePath);
            request.setAttribute("httpMethod", httpMethod);
            request.setAttribute("urlParams", pathParams);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-found.jsp");
            dispatcher.forward(request, response);
            
        } else {
            // Aucun mapping trouvé
            request.setAttribute("url", resourcePath);
            request.setAttribute("httpMethod", httpMethod);
            request.setAttribute("mappings", router.getMappings());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-not-found.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    /**
     * Sprint 8: Invoque la méthode du contrôleur avec injection automatique
     * Supporte:
     * - Map<String, Object> (injection complète des paramètres)
     * - Types primitifs individuels (int, double, String, etc.)
     */
    private Object invokeMethodWithParams(Mapping mapping, java.util.Map<String, Object> allParams) throws Exception {
        Method method = mapping.getMethod();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        if (paramTypes.length == 0) {
            // Méthode sans paramètre
            return method.invoke(mapping.getControllerClass().newInstance());
        }
        
        // Récupérer les noms des paramètres de l'URL pattern
        List<String> urlParamNames = (mapping.getUrlPattern() != null) 
            ? mapping.getUrlPattern().getParamNames() 
            : new java.util.ArrayList<>();
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            
            // ✅ Sprint 8: CAS 1 - Injection d'une Map<String, Object> complète
            if (java.util.Map.class.isAssignableFrom(paramType)) {
                args[i] = allParams;
                System.out.println("Sprint 8: Injection de Map<String, Object> avec " + allParams.size() + " paramètres");
                continue;
            }
            
            // CAS 2: Injection de paramètres individuels
            String paramName = null;
            Object paramValue = null;
            
            // Essayer de récupérer depuis l'URL pattern
            if (i < urlParamNames.size()) {
                paramName = urlParamNames.get(i);
                paramValue = allParams.get(paramName);
            }
            
            // Sinon, essayer avec reflection
            if (paramValue == null) {
                try {
                    java.lang.reflect.Parameter[] methodParams = method.getParameters();
                    if (i < methodParams.length) {
                        paramName = methodParams[i].getName();
                        paramValue = allParams.get(paramName);
                    }
                } catch (Exception e) {
                    System.err.println("WARN: Impossible d'obtenir le nom du paramètre via reflection");
                }
            }
            
            // Si toujours null, erreur
            if (paramValue == null) {
                throw new ServletException(
                    "Paramètre manquant: '" + (paramName != null ? paramName : "param" + i) + "' " +
                    "pour la méthode " + method.getDeclaringClass().getSimpleName() + "." + 
                    method.getName() + "(). " +
                    "Paramètres disponibles: " + allParams.keySet()
                );
            }
            
            // Conversion du type
            args[i] = convertParameter(paramValue, paramType, paramName);
        }
        
        return method.invoke(mapping.getControllerClass().newInstance(), args);
    }
    
    /**
     * Convertit un paramètre vers le type attendu
     */
    private Object convertParameter(Object value, Class<?> targetType, String paramName) 
            throws ServletException {
        
        if (value == null) {
            return null;
        }
        
        String stringValue = value.toString().trim();
        
        try {
            // Types numériques
            if (targetType == int.class || targetType == Integer.class) {
                return stringValue.isEmpty() ? 0 : Integer.parseInt(stringValue);
            }
            if (targetType == long.class || targetType == Long.class) {
                return stringValue.isEmpty() ? 0L : Long.parseLong(stringValue);
            }
            if (targetType == double.class || targetType == Double.class) {
                return stringValue.isEmpty() ? 0.0 : Double.parseDouble(stringValue);
            }
            if (targetType == float.class || targetType == Float.class) {
                return stringValue.isEmpty() ? 0.0f : Float.parseFloat(stringValue);
            }
            if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(stringValue);
            }
            
            // String
            if (targetType == String.class) {
                return stringValue;
            }
            
            // Par défaut, retourner la valeur brute
            return value;
            
        } catch (NumberFormatException e) {
            throw new ServletException(
                "Impossible de convertir '" + stringValue + "' en " + 
                targetType.getSimpleName() + " pour le paramètre '" + paramName + "'"
            );
        }
    }
}