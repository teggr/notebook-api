package com.teggr.notebook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "notebook")
public class NotebookProperties {
    private String notesDir = System.getProperty("user.home") + "/notebook/notes";
    private List<String> corsAllowedOrigins = List.of("http://localhost:5173");
    private List<String> corsAllowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private List<String> corsAllowedHeaders = List.of("*");

    public String getNotesDir() { return notesDir; }
    public void setNotesDir(String notesDir) { this.notesDir = notesDir; }

    public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) { this.corsAllowedOrigins = corsAllowedOrigins; }

    public List<String> getCorsAllowedMethods() { return corsAllowedMethods; }
    public void setCorsAllowedMethods(List<String> corsAllowedMethods) { this.corsAllowedMethods = corsAllowedMethods; }

    public List<String> getCorsAllowedHeaders() { return corsAllowedHeaders; }
    public void setCorsAllowedHeaders(List<String> corsAllowedHeaders) { this.corsAllowedHeaders = corsAllowedHeaders; }
}
