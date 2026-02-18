package com.perfumeshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("loginName")
    public String loginName(HttpSession session) {
        Object v = session.getAttribute("LOGIN_NAME");
        return v == null ? "" : v.toString();
    }

    @ModelAttribute("loginRole")
    public String loginRole(HttpSession session) {
        Object v = session.getAttribute("LOGIN_ROLE");
        return v == null ? "" : v.toString();
    }
}