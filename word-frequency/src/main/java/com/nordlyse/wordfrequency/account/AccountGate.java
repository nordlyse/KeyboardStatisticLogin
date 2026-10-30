package com.nordlyse.wordfrequency.account;

import org.springframework.web.servlet.HandlerInterceptor;

import com.nordlyse.wordfrequency.keystrike.ActiveProfile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class AccountGate implements HandlerInterceptor {

    static final String ACCOUNT = "account";
    static final String TOKEN = "accountToken";

    private final AccountService accounts;

    AccountGate(AccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if ("POST".equalsIgnoreCase(request.getMethod()) && isOpen(path(request))) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = header.substring("Bearer ".length()).trim();
        String username = accounts.usernameFor(token).orElse(null);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        request.setAttribute(ACCOUNT, username);
        request.setAttribute(TOKEN, token);
        ActiveProfile.use(username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ActiveProfile.clear();
    }

    private static boolean isOpen(String path) {
        return "/sessions".equals(path) || "/accounts".equals(path);
    }

    private static String path(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isEmpty()) {
            return servletPath;
        }
        return request.getRequestURI();
    }
}
