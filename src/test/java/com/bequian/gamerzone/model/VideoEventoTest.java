package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class VideoEventoTest {

    private static Evento evento() {
        return new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", List.of("GamerZone"), new ServiciosEvento(true, true, false, false),
                List.of("Expositor"), false);
    }

    private static Usuario usuario() {
        return new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));
    }

    @Test
    void guardaSoloElIdDelVideo() {
        Evento evento = evento();
        VideoEvento video = new VideoEvento(evento, usuario(), "dQw4w9WgXcQ", "Tráiler");

        assertThat(video.getEvento()).isSameAs(evento);
        assertThat(video.getYoutubeId()).isEqualTo("dQw4w9WgXcQ");
        assertThat(video.getTitulo()).isEqualTo("Tráiler");
        assertThat(video.getFecha()).isNotNull();
        assertThat(video.getId()).isNull();
    }
}
