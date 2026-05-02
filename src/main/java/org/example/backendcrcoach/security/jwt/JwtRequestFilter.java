package org.example.backendcrcoach.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backendcrcoach.security.user.CustomUserDetailsService;
import org.example.backendcrcoach.services.TokenBlacklistService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(CustomUserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String username = null;
        String jwt = null;

        // Buscar el token JWT en el encabezado Authorization
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                limpiarCookie(response);
                return;
            }
        } else
            // Buscar el token JWT en las cookies
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) { // Nombre de la cookie
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }


        // Validar y autenticar el token si se obtuvo
        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (IllegalArgumentException | io.jsonwebtoken.JwtException e) {
                // Token inválido, expirado o con firma no reconocida (posible reinicio del servidor
                // con clave distinta). Limpiar cookie en la respuesta para que el cliente no siga
                // manteniendo un token inválido.
                limpiarCookie(response);
                // No intentamos autenticar
                chain.doFilter(request, response);
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException ex) {
                limpiarCookie(response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (jwtUtil.validateToken(jwt, userDetails)) {
                var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continuar con el filtro
        chain.doFilter(request, response);
    }

    // Método para limpiar la cookie
    private void limpiarCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(0); // Caducar inmediatamente
        response.addCookie(jwtCookie);
    }
}
