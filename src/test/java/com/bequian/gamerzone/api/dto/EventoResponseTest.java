package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.ServiciosEvento;

class EventoResponseTest {

    @Test
    void desdeCopiaLosDatosDelEvento() {
        List<String> organizadores = new ArrayList<>(List.of("GamerZone"));
        Evento evento = new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", organizadores, new ServiciosEvento(true, false, true, false),
                List.of("Expositor"), true);

        EventoResponse r = EventoResponse.desde(evento);

        assertThat(r.titulo()).isEqualTo("LAN");
        assertThat(r.categoria()).isEqualTo("LAN Party");
        assertThat(r.fecha()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(r.organizadores()).containsExactly("GamerZone");
        assertThat(r.expositores()).containsExactly("Expositor");
        assertThat(r.servicios().comida()).isTrue();
        assertThat(r.destacado()).isTrue();
        // Las listas son copias inmutables: no exponen la entidad JPA
        assertThat(r.organizadores()).isUnmodifiable();
    }
}
