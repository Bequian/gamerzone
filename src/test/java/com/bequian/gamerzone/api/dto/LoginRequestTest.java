package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class LoginRequestTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validaCamposObligatoriosYLargoMaximo() {
        assertThat(VALIDADOR.validate(new LoginRequest("admin", "secreto"))).isEmpty();
        assertThat(VALIDADOR.validate(new LoginRequest("", ""))).hasSize(2);
        assertThat(VALIDADOR.validate(new LoginRequest("a".repeat(101), "b".repeat(73)))).hasSize(2);
    }
}
