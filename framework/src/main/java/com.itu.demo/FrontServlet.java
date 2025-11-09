package com.itu.demo;

import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.util.Map;

// import mg.framework.Router;
// import mg.framework.Mapping;

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
            request.setAttribute("mapping", mapping);
            request.setAttribute("url", resourcePath);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-found.jsp");
            dispatcher.forward(request, response);
        } else {
            request.setAttribute("url", resourcePath);
            request.setAttribute("mappings", router.getUrlMappings());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mapping-not-found.jsp");
            dispatcher.forward(request, response);
        }
    }
}