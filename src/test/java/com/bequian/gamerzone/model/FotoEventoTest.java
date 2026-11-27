package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class FotoEventoTest {

    private static Evento evento() {
        return new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", List.of("GamerZone"), new ServiciosEvento(true, true, false, false),
                List.of("Expositor"), false);
    }

    private static Usuario usuario() {
        return new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));
    }

    @Test
    void guardaLaImagenValidada() {
        Evento evento = evento();
        FotoEvento foto = new FotoEvento(evento, usuario(), "image/png", new byte[] {1, 2, 3});

        assertThat(foto.getEvento()).isSameAs(evento);
        assertThat(foto.getTipoContenido()).isEqualTo("image/png");
        assertThat(foto.getDatos()).containsExactly(1, 2, 3);
        assertThat(foto.getFecha()).isNotNull();
        assertThat(foto.getId()).isNull();
        assertThat(FotoEvento.TAMANO_MAXIMO).isEqualTo(2 * 1024 * 1024);
    }
}
