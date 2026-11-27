package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ComentarioTest {

    private static Evento evento() {
        return new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", List.of("GamerZone"), new ServiciosEvento(true, true, false, false),
                List.of("Expositor"), false);
    }

    private static Usuario usuario() {
        return new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));
    }

    @Test
    void creaYActualizaElComentario() {
        Evento evento = evento();
        Usuario autor = usuario();
        Comentario comentario = new Comentario(evento, autor, "Bueno", 3);

        assertThat(comentario.getEvento()).isSameAs(evento);
        assertThat(comentario.getUsuario()).isSameAs(autor);
        assertThat(comentario.getFecha()).isNotNull();
        assertThat(comentario.getId()).isNull();   // lo asigna la BD al guardar

        comentario.actualizar("Excelente", 5);
        assertThat(comentario.getTexto()).isEqualTo("Excelente");
        assertThat(comentario.getValoracion()).isEqualTo(5);
    }
}
