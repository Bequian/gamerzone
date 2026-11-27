package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FotoResponseTest {

    @Test
    void guardaIdYUrl() {
        FotoResponse foto = new FotoResponse(7L, "/api/eventos/1/fotos/7");
        assertThat(foto.id()).isEqualTo(7L);
        assertThat(foto.url()).endsWith("/fotos/7");
    }
}
