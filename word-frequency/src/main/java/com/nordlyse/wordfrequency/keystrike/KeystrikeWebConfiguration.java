package com.nordlyse.wordfrequency.keystrike;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class KeystrikeWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/keystrikes")
                .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081")
                .allowedMethods("POST")
                .allowedHeaders("Content-Type");
    }
}
