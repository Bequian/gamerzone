package com.bequian.gamerzone.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // activa @PreAuthorize en los controladores
public class WebSecurityConfig {

    // Las contraseñas se leen desde application.properties / variables de entorno
    @Value("${gamerzone.user.password}")
    private String userPassword;

    @Value("${gamerzone.admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> requests
                // Páginas públicas
                .requestMatchers("/", "/home", "/login", "/error").permitAll()
                // Recursos estáticos (CSS, JS, imágenes)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Zona administración: rol ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
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
                    "frame-ancestors 'none'; " +
                    "form-action 'self'"))
                .referrerPolicy(ref -> ref.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.deny())
            );

        return http.build();
    }

    // Usuarios en memoria (todavía no hay base de datos)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.builder()
            .username("user")
            .password(encoder.encode(userPassword))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(encoder.encode(adminPassword))
            .roles("USER", "ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}