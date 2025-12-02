package com.example.fiszapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Ensure that /api/** paths are handled by controllers, not as static resources
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /favicon.ico to the static resource location
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }
}
