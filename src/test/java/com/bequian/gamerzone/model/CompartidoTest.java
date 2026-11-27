package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CompartidoTest {

    private static Evento evento() {
        return new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1), LocalTime.NOON,
                "Santiago", "Calle 1", List.of("GamerZone"), new ServiciosEvento(true, true, false, false),
                List.of("Expositor"), false);
    }

    private static Usuario usuario() {
        return new Usuario("ana@test.cl", "hash", "Ana", Set.of("USER"));
    }

    @Test
    void guardaEventoUsuarioYFecha() {
        Evento evento = evento();
        Usuario usuario = usuario();
        Compartido compartido = new Compartido(evento, usuario);

        assertThat(compartido.getEvento()).isSameAs(evento);
        assertThat(compartido.getUsuario()).isSameAs(usuario);
        assertThat(compartido.getFecha()).isNotNull();
        assertThat(compartido.getId()).isNull();
    }
}
