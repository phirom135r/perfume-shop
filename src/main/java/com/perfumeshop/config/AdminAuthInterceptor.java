package com.perfumeshop.config;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        // ✅ allow auth endpoints
        if (uri.startsWith("/auth")) return true;

        // protect admin area
        if (uri.startsWith("/admin")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("LOGIN_NAME") == null) {
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return false;
            }
        }

        return true;
    }
}