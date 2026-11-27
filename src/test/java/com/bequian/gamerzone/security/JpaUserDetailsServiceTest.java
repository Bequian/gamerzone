package com.bequian.gamerzone.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
class JpaUserDetailsServiceTest {

    @Autowired
    private JpaUserDetailsService service;

    @Test
    void cargaUsuarioNormalizandoElNombreYConSusRoles() {
        UserDetails admin = service.loadUserByUsername("  ADMIN ");
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getPassword()).startsWith("$2");   // hash BCrypt
        assertThat(admin.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void usuarioInexistenteONuloLanzaExcepcion() {
        assertThatThrownBy(() -> service.loadUserByUsername("no-existe"))
                .isInstanceOf(UsernameNotFoundException.class);
        assertThatThrownBy(() -> service.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
