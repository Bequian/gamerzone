package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CatalogoTest {

    @Test
    void listasBlancasDeAvataresYJuegos() {
        assertThat(Catalogo.AVATARES).containsKey("control").hasSize(8);
        assertThat(Catalogo.AVATARES).isUnmodifiable();
        assertThat(Catalogo.JUEGOS).contains("Minecraft", "Valorant").isUnmodifiable();
    }
}
