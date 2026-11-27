package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServiciosEventoTest {

    @Test
    void guardaCadaServicio() {
        ServiciosEvento servicios = new ServiciosEvento(true, false, true, false);
        assertThat(servicios.estacionamiento()).isTrue();
        assertThat(servicios.locomocion()).isFalse();
        assertThat(servicios.comida()).isTrue();
        assertThat(servicios.alojamiento()).isFalse();
        assertThat(servicios).isEqualTo(new ServiciosEvento(true, false, true, false));
    }
}
