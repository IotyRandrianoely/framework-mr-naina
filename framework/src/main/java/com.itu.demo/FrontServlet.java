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
                System.out.println("Pattern: " + mapping.getUrlPattern().getPattern() + " -> " + mapping);
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
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String resourcePath = requestURI.substring(contextPath.length());
        
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
        
        Mapping mapping = router.getMapping(resourcePath);
        
        if (mapping != null) {
            // Extraire les paramètres d'URL (path params: /etudiant/{id})
            Map<String, String> params = router.extractParams(resourcePath, mapping);
            
            // Ajouter les paramètres de requête (query params: ?id=2)
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                params.put(paramName, request.getParameter(paramName));
            }
            
            try {
                // Invoquer la méthode avec injection des paramètres
                Object result = invokeMethodWithParams(mapping, params);
                
                // Passer le résultat au JSP
                if (result instanceof ModelView) {
                    ModelView mv = (ModelView) result;
                    if (mv.getData() != null) {
                        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                    request.setAttribute("urlParams", params);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/" + mv.getView());
                    dispatcher.forward(request, response);
                    return;
                } else if (result instanceof String) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println(result);
                    return;
                }
            } catch (Exception e) {
                throw new ServletException("Erreur d'invocation", e);
            }
            
            request.setAttribute("mapping", mapping);
            request.setAttribute("url", resourcePath);
            request.setAttribute("urlParams", params);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-found.jsp");
            dispatcher.forward(request, response);
        } else {
            request.setAttribute("url", resourcePath);
            request.setAttribute("mappings", router.getMappings());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-not-found.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    // Nouvelle méthode pour invoquer avec injection des paramètres
    private Object invokeMethodWithParams(Mapping mapping, Map<String, String> params) throws Exception {
        Method method = mapping.getMethod();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        if (paramTypes.length > 0) {
            List<String> paramNames = (mapping.getUrlPattern() != null) 
                ? mapping.getUrlPattern().getParamNames() 
                : new java.util.ArrayList<>();
            
            for (int i = 0; i < paramTypes.length; i++) {
                String paramName = null;
                String paramValue = null;
                
                // 1) Chercher dans les paramètres du pattern d'URL (ex: {id})
                if (i < paramNames.size()) {
                    paramName = paramNames.get(i);
                    paramValue = params.get(paramName);
                }
                
                // 2) Si pas trouvé, chercher par le nom du paramètre de la méthode via reflection
                if (paramValue == null && paramName == null) {
                    try {
                        java.lang.reflect.Parameter[] methodParams = method.getParameters();
                        if (i < methodParams.length) {
                            paramName = methodParams[i].getName();
                            paramValue = params.get(paramName);
                        }
                    } catch (Exception e) {
                        System.out.println("WARN: Impossible d'accéder aux noms de paramètres via reflection. Assurez-vous de compiler avec -parameters");
                    }
                }
                
                // 3) Vérifier si le paramètre a été trouvé
                if (paramValue == null) {
                    throw new ServletException(
                        "Paramètre manquant: '" + (paramName != null ? paramName : "arg" + i) + "' " +
                        "pour la méthode " + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(). " +
                        "Paramètres disponibles: " + params.keySet()
                    );
                }
                
                // 4) Convertir et assigner la valeur
                try {
                    if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                        args[i] = Integer.parseInt(paramValue);
                    } else if (paramTypes[i] == String.class) {
                        args[i] = paramValue;
                    } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
                        args[i] = Double.parseDouble(paramValue);
                    } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                        args[i] = Boolean.parseBoolean(paramValue);
                    } else if (paramTypes[i] == long.class || paramTypes[i] == Long.class) {
                        args[i] = Long.parseLong(paramValue);
                    }
                } catch (NumberFormatException ex) {
                    throw new ServletException(
                        "Conversion échouée: Impossible de convertir '" + paramValue + 
                        "' en " + paramTypes[i].getSimpleName() + 
                        " pour le paramètre '" + paramName + "'"
                    );
                }
            }
        }
        
        return method.invoke(mapping.getControllerClass().newInstance(), args);
    }
}