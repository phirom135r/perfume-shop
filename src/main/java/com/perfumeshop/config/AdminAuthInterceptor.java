package com.perfumeshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        boolean loggedIn = session != null && session.getAttribute("LOGIN_USERNAME") != null;

        if (!loggedIn) {
            response.sendRedirect("/auth/login");
            return false;
        }

        // optional: allow only ADMIN role
        Object role = session.getAttribute("LOGIN_ROLE");
        if (role != null && !"ADMIN".equals(String.valueOf(role))) {
            response.sendRedirect("/auth/login");
            return false;
        }

        return true;
    }
}