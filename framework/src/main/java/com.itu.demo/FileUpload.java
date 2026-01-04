package com.itu.demo;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Sprint 10: Classe représentant un fichier uploadé
 */
public class FileUpload {
    
    private String fileName;
    private String contentType;
    private byte[] bytes;
    private long size;
    
    public FileUpload() {
    }
    
    public FileUpload(String fileName, String contentType, byte[] bytes) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;
        this.size = bytes != null ? bytes.length : 0;
    }
    
    /**
     * Crée un FileUpload depuis une Part de servlet
     */
    public static FileUpload fromPart(javax.servlet.http.Part part) throws IOException {
        if (part == null) {
            return null;
        }
        
        // Récupérer le nom du fichier
        String fileName = extractFileName(part);
        
        // Récupérer le content type
        String contentType = part.getContentType();
        
        // Lire les bytes du fichier
        byte[] bytes = readBytes(part.getInputStream());
        
        return new FileUpload(fileName, contentType, bytes);
    }
    
    /**
     * Extrait le nom du fichier depuis le header Content-Disposition
     */
    private static String extractFileName(javax.servlet.http.Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return "unknown";
        }
        
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                String fileName = token.substring(token.indexOf('=') + 1).trim()
                    .replace("\"", "");
                // Extraire seulement le nom du fichier (pas le chemin complet)
                int lastIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
                if (lastIndex >= 0) {
                    fileName = fileName.substring(lastIndex + 1);
                }
                return fileName;
            }
        }
        return "unknown";
    }
    
    /**
     * Lit tous les bytes depuis un InputStream
     */
    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        
        buffer.flush();
        return buffer.toByteArray();
    }
    
    // Getters et Setters
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        this.size = bytes != null ? bytes.length : 0;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    /**
     * Retourne la taille du fichier en format lisible
     */
    public String getFormattedSize() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return "FileUpload{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + getFormattedSize() +
                '}';
    }
}
