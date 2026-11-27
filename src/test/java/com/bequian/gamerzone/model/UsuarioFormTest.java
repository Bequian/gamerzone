package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class UsuarioFormTest {

    @Test
    void desdeCargaLosDatosSinLaContrasena() {
        Usuario usuario = new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER", "ADMIN"));

        UsuarioForm form = UsuarioForm.desde(usuario);

        assertThat(form.getNombre()).isEqualTo("Ana");
        assertThat(form.getCorreo()).isEqualTo("ana@test.cl");
        assertThat(form.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(form.getPassword()).isNull();   // el hash nunca se lleva al formulario
    }

    @Test
    void rolesNulosSeConviertenEnListaVacia() {
        UsuarioForm form = new UsuarioForm();
        form.setPassword("Clave1234");
        form.setRoles(null);
        assertThat(form.getRoles()).isEmpty();
        assertThat(form.getPassword()).isEqualTo("Clave1234");
    }
}
