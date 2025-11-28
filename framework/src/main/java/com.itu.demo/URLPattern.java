package com.itu.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLPattern {
    private String pattern;           // Ex: /etudiant/{id}
    private Pattern regex;            // Pattern regex compilé
    private List<String> paramNames;  // Liste des noms de paramètres

    public URLPattern(String pattern) {
        this.pattern = pattern;
        this.paramNames = new ArrayList<>();
        this.regex = compilePattern(pattern);
    }

    /**
     * Convertit un pattern d'URL en regex
     * Ex: /etudiant/{id} -> /etudiant/([^/]+)
     */
    private Pattern compilePattern(String urlPattern) {
        // Extraire les noms de paramètres
        Matcher matcher = Pattern.compile("\\{([^}]+)\\}").matcher(urlPattern);
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }

        // Convertir le pattern en regex
        String regex = urlPattern.replaceAll("\\{[^}]+\\}", "([^/]+)");
        regex = "^" + regex + "$";
        
        return Pattern.compile(regex);
    }

    /**
     * Vérifie si une URL correspond au pattern
     */
    public boolean matches(String url) {
        return regex.matcher(url).matches();
    }

    /**
     * Extrait les paramètres d'une URL
     */
    public Map<String, String> extractParams(String url) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = regex.matcher(url);
        
        if (matcher.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                params.put(paramNames.get(i), matcher.group(i + 1));
            }
        }
        
        return params;
    }

    public String getPattern() {
        return pattern;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    @Override
    public String toString() {
        return "URLPattern{pattern='" + pattern + "', params=" + paramNames + "}";
    }
}