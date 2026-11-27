package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ComentarioRequestTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceptaComentarioValido() {
        assertThat(VALIDADOR.validate(new ComentarioRequest("Buen evento", 5))).isEmpty();
    }

    @Test
    void rechazaTextoVacioYValoracionFueraDeRango() {
        assertThat(VALIDADOR.validate(new ComentarioRequest(" ", 0))).hasSize(2);
        assertThat(VALIDADOR.validate(new ComentarioRequest("a".repeat(501), 6))).hasSize(2);
        assertThat(VALIDADOR.validate(new ComentarioRequest("Hola", null))).hasSize(1);
    }
}
