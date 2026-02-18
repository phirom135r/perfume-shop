package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.User;
import com.perfumeshop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AdminAuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    public AdminAuthController(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session) {

        User u = userRepo.findByUsername(username).orElse(null);

        if (u == null || !u.isActive() || !encoder.matches(password, u.getPasswordHash())) {
            return "redirect:/auth/login?error=true";
        }

        // session set (for interceptor)
        session.setAttribute("LOGIN_NAME", u.getFullName());
        session.setAttribute("LOGIN_ROLE", u.getRole());
        session.setAttribute("LOGIN_USERNAME", u.getUsername());

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
