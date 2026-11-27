package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class VideoRequestTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void urlEsObligatoriaYTituloOpcional() {
        assertThat(VALIDADOR.validate(new VideoRequest("https://youtu.be/dQw4w9WgXcQ", null))).isEmpty();
        assertThat(VALIDADOR.validate(new VideoRequest("", "Título"))).hasSize(1);
        assertThat(VALIDADOR.validate(new VideoRequest("https://youtu.be/x" + "a".repeat(300), "t".repeat(101))))
                .hasSize(2);
    }
}
