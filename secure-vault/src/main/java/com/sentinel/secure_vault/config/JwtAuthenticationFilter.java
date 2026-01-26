package com.sentinel.secure_vault.config;

import com.sentinel.secure_vault.service.CustomUserDetailsService;
import com.sentinel.secure_vault.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // --- DEBUG LOGS ---
        String authHeader = request.getHeader("Authorization");
        System.out.println("🕵️ FILTER: Request to " + request.getRequestURI());
        System.out.println("   > Header: " + authHeader);
        // ------------------

        // 1. Get the Authorization Header
//        String authHeader = request.getHeader("Authorization");
        System.out.println("1. Header received: " + authHeader);
        String token = null;
        String username = null;

        // 2. Check if it starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove "Bearer " prefix
            username = jwtUtil.extractUsername(token);
            System.out.println("2. Token extracted. Username: " + username);
        }
        else {
            System.out.println("2. Header is missing 'Bearer ' prefix!");
        }

        // 3. If we have a username and they are NOT already logged in
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Load the user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Validate the token
            if (jwtUtil.validateToken(token, userDetails.getUsername())) {

                // 6. Create the Authentication Object (The "Stamped Passport")
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Set it in the Context (Log them in!)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}