package com.bequian.gamerzone.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // activa @PreAuthorize en los controladores
public class WebSecurityConfig {

    /**
     * Cadena 1: API REST (/api/**), autenticación con JWT.
     * @Order(1): se evalúa primero; solo aplica a las rutas que empiezan con /api/.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      JwtService jwtService,
                                                      UserDetailsService userDetailsService) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(requests -> requests
                // APIs públicas
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/eventos").permitAll()
                // Gestión de eventos: solo ORGANIZADOR o ADMIN
                .requestMatchers(HttpMethod.POST, "/api/eventos").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/eventos/*").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/*").hasAnyRole("ORGANIZADOR", "ADMIN")
                // Fotos y videos: agregar o eliminar solo ORGANIZADOR o ADMIN
                .requestMatchers(HttpMethod.POST, "/api/eventos/*/fotos", "/api/eventos/*/videos")
                    .hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/*/fotos/*", "/api/eventos/*/videos/*")
                    .hasAnyRole("ORGANIZADOR", "ADMIN")
                // Comentarios, valoraciones y compartir: cualquier usuario con token (regla anyRequest)
                // Cualquier otra API (por ejemplo el detalle de un evento) requiere un token válido
                .anyRequest().authenticated()
            )
            // Sin sesión en el servidor: cada petición debe traer su token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF desactivado SOLO en la API: el token viaja en una cabecera que el navegador
            // no envía automáticamente (a diferencia de una cookie), por lo que CSRF no aplica
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            // Respuestas JSON en vez de redirigir a la página de login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) ->
                    escribirError(response, 401, "Token ausente, inválido o expirado"))
                .accessDeniedHandler((request, response, e) ->
                    escribirError(response, 403, "No tienes permisos para esta operación"))
            )
            // El filtro JWT se ejecuta antes del filtro estándar de usuario y contraseña
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cadena 2: sitio web con Thymeleaf, autenticación con formulario y sesión (igual que antes).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> requests
                // Páginas públicas
                .requestMatchers("/", "/home", "/login", "/registro", "/error").permitAll()
                // Búsqueda de eventos: pública. El detalle (/eventos/{id}) sigue siendo privado
                .requestMatchers(HttpMethod.GET, "/eventos").permitAll()
                // Recursos estáticos (CSS, JS, imágenes)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Zona administración: rol ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Agregar o eliminar fotos y videos de un evento: solo ORGANIZADOR o ADMIN
                .requestMatchers(HttpMethod.POST, "/eventos/*/fotos", "/eventos/*/videos",
                                 "/eventos/*/fotos/*/eliminar", "/eventos/*/videos/*/eliminar")
                    .hasAnyRole("ORGANIZADOR", "ADMIN")
                // Todo lo demás requiere iniciar sesión
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Cabeceras HTTP de seguridad
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "img-src 'self' data:; " +
                    "style-src 'self'; " +
                    "script-src 'self'; " +
                    // Solo se pueden incrustar videos desde el modo privado de YouTube
                    "frame-src https://www.youtube-nocookie.com; " +
                    "frame-ancestors 'none'; " +
                    "form-action 'self'"))
                .referrerPolicy(ref -> ref.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.deny())
            );

        return http.build();
    }

    // Lo usa AuthController para verificar usuario y contraseña contra la BD
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escribirError(jakarta.servlet.http.HttpServletResponse response,
                                      int status, String mensaje) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}