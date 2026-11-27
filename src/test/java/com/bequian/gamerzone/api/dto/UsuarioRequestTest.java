package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class UsuarioRequestTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceptaUsuarioValido() {
        assertThat(VALIDADOR.validate(new UsuarioRequest("Ana", "ana@test.cl", "Clave1234", List.of("USER"))))
                .isEmpty();
    }

    @Test
    void rechazaCorreoInvalidoContrasenaDebilYRolesNulos() {
        assertThat(VALIDADOR.validate(new UsuarioRequest("", "no-es-correo", "solotexto", null)))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("nombre", "correo", "password", "roles");
    }
}
