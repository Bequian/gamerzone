package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.bequian.gamerzone.model.Usuario;

class UsuarioResponseTest {

    @Test
    void desdeOrdenaLosRolesYNoIncluyeLaContrasena() {
        Usuario usuario = new Usuario("ana@test.cl", "$2a$hash", "Ana", Set.of("USER", "ADMIN", "ORGANIZADOR"));

        UsuarioResponse r = UsuarioResponse.desde(usuario);

        assertThat(r.username()).isEqualTo("ana@test.cl");
        assertThat(r.nombre()).isEqualTo("Ana");
        assertThat(r.roles()).containsExactly("ADMIN", "ORGANIZADOR", "USER");
        assertThat(r.toString()).doesNotContain("$2a$hash");
    }
}
