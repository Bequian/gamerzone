package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RegistroFormTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static RegistroForm form(String nombre, String correo, String password) {
        RegistroForm form = new RegistroForm();
        form.setNombre(nombre);
        form.setCorreo(correo);
        form.setPassword(password);
        form.setConfirmarPassword(password);
        return form;
    }

    @Test
    void aceptaDatosValidos() {
        RegistroForm valido = form("Ana", "ana@test.cl", "Clave1234");
        assertThat(VALIDADOR.validate(valido)).isEmpty();
        assertThat(valido.getConfirmarPassword()).isEqualTo("Clave1234");
    }

    @Test
    void rechazaCorreoInvalidoYContrasenaSinNumeros() {
        assertThat(VALIDADOR.validate(form("Ana", "ana", "solamenteletras")))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("correo", "password");
    }
}
