package com.teggr.notebook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final NotebookProperties notebookProperties;

    public WebConfig(NotebookProperties notebookProperties) {
        this.notebookProperties = notebookProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(notebookProperties.getCorsAllowedOrigins().toArray(String[]::new))
                .allowedMethods(notebookProperties.getCorsAllowedMethods().toArray(String[]::new))
                .allowedHeaders(notebookProperties.getCorsAllowedHeaders().toArray(String[]::new));
    }
}