package com.perfumeshop.config;

import com.perfumeshop.entity.Customer;
import com.perfumeshop.repository.CustomerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final CustomerRepository customerRepository;

    public GoogleOAuth2SuccessHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oauthUser.getAttributes();

        String email = (String) attrs.get("email");
        String name = (String) attrs.get("name");
        String sub = (String) attrs.get("sub");

        Customer customer = customerRepository.findByEmail(email).orElse(null);

        if (customer == null) {
            customer = new Customer();
            customer.setEmail(email);
            customer.setFullName(name);
            customer.setProvider("GOOGLE");
            customer.setProviderId(sub);
            customer.setEmailVerified(true);
            customer.setActive(true);

            customerRepository.save(customer);
        }

        // IMPORTANT: create new Authentication using EMAIL
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        response.sendRedirect("/perfume-shop");
    }
}