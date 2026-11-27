package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void constructorCortoDejaLosErroresVacios() {
        ErrorResponse error = new ErrorResponse(404, "No encontrado");
        assertThat(error.status()).isEqualTo(404);
        assertThat(error.mensaje()).isEqualTo("No encontrado");
        assertThat(error.errores()).isEmpty();
    }

    @Test
    void constructorCompletoGuardaElDetalle() {
        assertThat(new ErrorResponse(400, "Datos inválidos", Map.of("titulo", "obligatorio")).errores())
                .containsEntry("titulo", "obligatorio");
    }
}
