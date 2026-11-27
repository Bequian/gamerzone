package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidacionExceptionTest {

    @Test
    void guardaElMensajeParaElUsuario() {
        ValidacionException e = new ValidacionException("Solo se permiten imágenes JPG o PNG");
        assertThat(e).isInstanceOf(RuntimeException.class)
                .hasMessage("Solo se permiten imágenes JPG o PNG");
    }
}
