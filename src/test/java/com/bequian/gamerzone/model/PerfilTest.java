package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PerfilTest {

    @Test
    void perfilPorDefecto() {
        Perfil perfil = Perfil.porDefecto();
        assertThat(perfil.gamertag()).isEmpty();
        assertThat(perfil.avatar()).isEqualTo("control");
        assertThat(perfil.juegos()).isEmpty();
        assertThat(perfil.notifEmail()).isTrue();
        assertThat(perfil.notifNuevosEventos()).isTrue();
        assertThat(perfil.notifRecordatorios()).isFalse();
    }
}
