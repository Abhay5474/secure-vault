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

        String requestPath = request.getRequestURI();

        // --- 1. CRITICAL CORS FIX: SKIP "OPTIONS" REQUESTS ---
        // Browsers send a "Preflight" OPTIONS request before the real POST/GET.
        // We must let this pass immediately without checking for a token.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }
        // -----------------------------------------------------

        // --- DEBUG LOGS ---
        String authHeader = request.getHeader("Authorization");
        System.out.println("🕵️ FILTER: Request to " + requestPath);
        // ------------------

        String token = null;
        String username = null;

        // 2. Check header and extract Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // If token is expired or garbage, just log it and continue.
                System.out.println("❌ Token extraction failed: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ No Bearer token found (Normal for public pages)");
        }

        // 3. If we have a username and they are NOT already logged in
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Load the user details from DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 5. Validate the token
            // ✅ FIXED LINE BELOW: using .getUsername()
            if (jwtUtil.validateToken(token, userDetails.getUsername())) {

                // 6. Create the Authentication Object
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