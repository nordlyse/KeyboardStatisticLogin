package com.nordlyse.wordfrequency.account;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AccountWebConfiguration implements WebMvcConfigurer {

    private final ObjectProvider<AccountService> accounts;

    public AccountWebConfiguration(ObjectProvider<AccountService> accounts) {
        this.accounts = accounts;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AccountService service = accounts.getIfAvailable();
        if (service == null) {
            return;
        }
        registry.addInterceptor(new AccountGate(service))
                .addPathPatterns(
                        "/keystrikes", "/keystrikes/**",
                        "/matches", "/matches/**",
                        "/lines", "/lines/**",
                        "/profile", "/profile/**",
                        "/sessions");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/accounts")
                .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081")
                .allowedMethods("POST")
                .allowedHeaders("Content-Type");
        registry.addMapping("/sessions")
                .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081")
                .allowedMethods("GET", "POST", "DELETE")
                .allowedHeaders("Content-Type", "Authorization");
        registry.addMapping("/lines")
                .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081")
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type", "Authorization");
        registry.addMapping("/profile")
                .allowedOrigins("http://localhost:8081", "http://127.0.0.1:8081")
                .allowedMethods("GET")
                .allowedHeaders("Content-Type", "Authorization");
    }
}
