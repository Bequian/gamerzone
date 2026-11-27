package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class EventoTest {

    @Test
    void constructorGuardaTodosLosDatos() {
        Evento evento = new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", List.of("GamerZone"), new ServiciosEvento(true, true, false, false),
                List.of("Expositor"), true);

        assertThat(evento.getTitulo()).isEqualTo("LAN");
        assertThat(evento.getDescripcion()).isEqualTo("Descripción");
        assertThat(evento.getHora()).isEqualTo(LocalTime.NOON);
        assertThat(evento.getDireccion()).isEqualTo("Calle 1");
        assertThat(evento.isDestacado()).isTrue();
    }

    @Test
    void actualizarReemplazaLosDatosYLasListas() {
        Evento evento = new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", List.of("GamerZone"), new ServiciosEvento(true, true, false, false),
                List.of("Expositor"), true);

        evento.actualizar("Torneo", "Torneo", "Nueva", LocalDate.of(2031, 5, 5), LocalTime.MIDNIGHT,
                "Valparaíso", "Calle 2", List.of("Liga"), new ServiciosEvento(false, false, true, true),
                List.of(), false);

        assertThat(evento.getTitulo()).isEqualTo("Torneo");
        assertThat(evento.getCategoria()).isEqualTo("Torneo");
        assertThat(evento.getFecha()).isEqualTo(LocalDate.of(2031, 5, 5));
        assertThat(evento.getUbicacion()).isEqualTo("Valparaíso");
        assertThat(evento.getOrganizadores()).containsExactly("Liga");
        assertThat(evento.getExpositores()).isEmpty();
        assertThat(evento.getServicios().alojamiento()).isTrue();
        assertThat(evento.isDestacado()).isFalse();
    }
}
