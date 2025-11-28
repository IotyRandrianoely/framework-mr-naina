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
            // Extraire les paramètres d'URL
            Map<String, String> params = router.extractParams(resourcePath, mapping);
            
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
            // Récupérer les noms des paramètres du pattern d'URL
            List<String> paramNames = mapping.getUrlPattern().getParamNames();
            
            for (int i = 0; i < paramTypes.length; i++) {
                String paramName = (i < paramNames.size()) ? paramNames.get(i) : null;
                String paramValue = (paramName != null) ? params.get(paramName) : null;
                
                if (paramValue != null) {
                    if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                        args[i] = Integer.parseInt(paramValue);
                    } else if (paramTypes[i] == String.class) {
                        args[i] = paramValue;
                    } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
                        args[i] = Double.parseDouble(paramValue);
                    } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                        args[i] = Boolean.parseBoolean(paramValue);
                    }
                }
            }
        }
        
        return method.invoke(mapping.getControllerClass().newInstance(), args);
    }
}