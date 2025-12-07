package com.amyway.luckydraw.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        
        // Simplified Auth Logic for Demo
        // In reality, verify JWT or OAuth token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // Mock Admin check: if token is "Bearer admin-secret", allow admin routes
        String token = authHeader.substring(7);
        if (request.getRequestURI().startsWith("/api/admin") && !"admin-secret".equals(token)) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             return false;
        }

        return true;
    }
}
