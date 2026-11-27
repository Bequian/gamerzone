package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.bequian.gamerzone.model.Marca;

class MarcaServiceTest {

    @Test
    void listaLasMarcasAsociadas() {
        assertThat(new MarcaService().listarTodas())
                .hasSize(4)
                .extracting(Marca::nombre)
                .contains("HexaGear", "PixelFuel");
    }
}
