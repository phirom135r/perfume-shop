package com.perfumeshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        // allow auth endpoints + static
        if (uri.startsWith("/auth")) return true;
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images")) return true;

        // protect admin
        if (uri.startsWith("/admin")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("LOGIN_NAME") == null) {
                response.sendRedirect("/auth/login");
                return false;
            }
        }
        return true;
    }
}