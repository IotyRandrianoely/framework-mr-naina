package com.itu.demo;

import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.util.Map;

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
            for (Map.Entry<String, Mapping> entry : router.getUrlMappings().entrySet()) {
                System.out.println("URL: " + entry.getKey() + " -> " + entry.getValue());
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
            try {
                // Vérifier si la méthode retourne un ModelView
                if (mapping.returnsModelView()) {
                    ModelView modelView = (ModelView) mapping.invokeMethod();
                    
                    // Copier les données du ModelView dans les attributs de la requête
                    if (modelView.getData() != null) {
                        for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                    
                    String viewPath = "/WEB-INF/views/" + modelView.getView();
                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
                    dispatcher.forward(request, response);
                    
                } else if (mapping.returnsString()) {
                    // Invoquer la méthode et afficher le résultat
                    String result = (String) mapping.invokeMethod();
                    
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    out.println("<!DOCTYPE html>");
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Resultat</title>");
                    out.println("<style>");
                    out.println("body { font-family: Arial, sans-serif; padding: 40px; background: #f5f5f5; }");
                    out.println(".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
                    out.println("h1 { color: #28a745; }");
                    out.println(".result { background: #e7f3ff; padding: 20px; border-radius: 5px; margin: 20px 0; }");
                    out.println(".info { color: #666; font-size: 0.9em; }");
                    out.println("</style>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<div class='container'>");
                    out.println("<h1>✓ Méthode invoquée avec succès</h1>");
                    out.println("<div class='info'><strong>URL:</strong> " + resourcePath + "</div>");
                    out.println("<div class='info'><strong>Classe:</strong> " + mapping.getControllerClass().getName() + "</div>");
                    out.println("<div class='info'><strong>Méthode:</strong> " + mapping.getMethod().getName() + "()</div>");
                    out.println("<div class='result'><h3>Résultat:</h3>" + result + "</div>");
                    out.println("</div>");
                    out.println("</body>");
                    out.println("</html>");
                    
                } else {
                    // Méthode ne retourne ni String ni ModelView - afficher seulement les infos
                    request.setAttribute("mapping", mapping);
                    request.setAttribute("url", resourcePath);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-found.jsp");
                    dispatcher.forward(request, response);
                }
                
            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'invocation de la méthode", e);
            }
        } else {
            request.setAttribute("url", resourcePath);
            request.setAttribute("mappings", router.getUrlMappings());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-not-found.jsp");
            dispatcher.forward(request, response);
        }
    }
}