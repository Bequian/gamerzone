package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ComentarioResponseTest {

    @Test
    void guardaLosDatosDelComentario() {
        LocalDateTime fecha = LocalDateTime.of(2026, 1, 1, 12, 0);
        ComentarioResponse c = new ComentarioResponse(1L, "Ana", "Genial", 5, fecha, true);
        assertThat(c.autor()).isEqualTo("Ana");
        assertThat(c.valoracion()).isEqualTo(5);
        assertThat(c.fecha()).isEqualTo(fecha);
        assertThat(c.propio()).isTrue();
        assertThat(c).isEqualTo(new ComentarioResponse(1L, "Ana", "Genial", 5, fecha, true));
    }
}
