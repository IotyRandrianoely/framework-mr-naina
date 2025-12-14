package com.itu.demo;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@WebFilter("/*")
public class HttpMethodFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("===== HttpMethodFilter initialisé =====");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String methodOverride = httpRequest.getParameter("_method");
        
        if (methodOverride != null && !methodOverride.isEmpty()) {
            String originalMethod = httpRequest.getMethod();
            System.out.println("HttpMethodFilter: Conversion " + originalMethod + " -> " + methodOverride.toUpperCase());
            HttpServletRequest wrappedRequest = new HttpMethodRequestWrapper(httpRequest, methodOverride);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        System.out.println("===== HttpMethodFilter détruit =====");
    }

    private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {
        private final String method;

        public HttpMethodRequestWrapper(HttpServletRequest request, String method) {
            super(request);
            this.method = method.toUpperCase();
        }

        @Override
        public String getMethod() {
            return this.method;
        }
    }
}
