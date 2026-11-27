package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MarcaTest {

    @Test
    void guardaLosDatosDeLaMarca() {
        Marca marca = new Marca("HexaGear", "Periféricos", "Teclados", "15% dcto.");
        assertThat(marca.nombre()).isEqualTo("HexaGear");
        assertThat(marca.rubro()).isEqualTo("Periféricos");
        assertThat(marca.mensaje()).isEqualTo("Teclados");
        assertThat(marca.oferta()).isEqualTo("15% dcto.");
    }
}
