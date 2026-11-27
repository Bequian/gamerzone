package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComentariosResponseTest {

    @Test
    void sinComentariosElPromedioEsNulo() {
        ComentariosResponse vacio = new ComentariosResponse(null, 0, List.of());
        assertThat(vacio.promedio()).isNull();
        assertThat(vacio.total()).isZero();
        assertThat(vacio.comentarios()).isEmpty();
    }
}
