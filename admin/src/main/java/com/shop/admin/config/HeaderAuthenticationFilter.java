package com.shop.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Trusts authentication headers set by the gateway (X-User-Id, X-User-Role, X-User-Name)
 * and builds a SecurityContext so Spring Security authorization checks work.
 */
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");
            String username = request.getHeader("X-User-Name");

            if (StringUtils.hasText(role)) {
                String principal = StringUtils.hasText(username) ? username : userId;
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var auth = new PreAuthenticatedAuthenticationToken(principal, "N/A", authorities);
                auth.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
