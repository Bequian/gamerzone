package com.bequian.gamerzone.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que se ejecuta en cada petición a /api/**.
 * Si viene la cabecera "Authorization: Bearer <token>" y el token es válido,
 * marca al usuario como autenticado para esta petición.
 *
 * NO lleva @Component a propósito: si lo tuviera, Spring Boot lo registraría también
 * para TODAS las peticiones de la web, fuera de la cadena de seguridad de la API.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        // Sin token: sigue como anónimo (las APIs públicas responden, las privadas devolverán 401)
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecera.substring(PREFIJO.length());

        try {
            String username = jwtService.extraerUsername(token);

            // Se consulta la BD: si el usuario fue eliminado o cambió de rol, se refleja de inmediato
            UserDetails usuario = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken autenticacion =
                    UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            // Token alterado, expirado o de un usuario inexistente: se trata como anónimo
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}