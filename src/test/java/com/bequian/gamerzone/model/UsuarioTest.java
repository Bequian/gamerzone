package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void creaUsuarioConPerfilPorDefecto() {
        Usuario usuario = new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));
        assertThat(usuario.getId()).isNull();
        assertThat(usuario.getUsername()).isEqualTo("ana@test.cl");
        assertThat(usuario.getPerfil().avatar()).isEqualTo("control");
        assertThat(usuario.getPerfil().gamertag()).isEmpty();
    }

    @Test
    void actualizaPerfilRolesNombreYContrasena() {
        Usuario usuario = new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));

        usuario.actualizarPerfil(new Perfil("Ana_GZ", "ninja", List.of("Fortnite"), false, false, true));
        usuario.setRoles(Set.of("USER", "ADMIN"));
        usuario.setNombre("Ana María");
        usuario.setPassword("otro-hash");

        assertThat(usuario.getPerfil()).isEqualTo(new Perfil("Ana_GZ", "ninja", List.of("Fortnite"), false, false, true));
        assertThat(usuario.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(usuario.getNombre()).isEqualTo("Ana María");
        assertThat(usuario.getPassword()).isEqualTo("otro-hash");
    }

    @Test
    void gamertagNuloSeMuestraVacio() {
        Usuario usuario = new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));
        usuario.actualizarPerfil(new Perfil(null, "control", List.of(), true, true, false));
        assertThat(usuario.getPerfil().gamertag()).isEmpty();
    }
}
