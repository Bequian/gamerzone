package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class UsuarioActualizarRequestTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void laContrasenaEsOpcional() {
        assertThat(VALIDADOR.validate(new UsuarioActualizarRequest("Ana", List.of("USER"), null))).isEmpty();
    }

    @Test
    void siVieneContrasenaDebeSerSegura() {
        assertThat(VALIDADOR.validate(new UsuarioActualizarRequest("Ana", List.of(), "12345678"))).hasSize(1);
        assertThat(VALIDADOR.validate(new UsuarioActualizarRequest(" ", null, null))).hasSize(2);
    }
}
