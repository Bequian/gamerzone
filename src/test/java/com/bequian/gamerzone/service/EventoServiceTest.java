package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Evento;

@SpringBootTest
class EventoServiceTest {

    @Autowired
    private EventoService servicio;

    @Test
    void buscarAplicaCadaFiltro() {
        assertThat(servicio.buscar("Torneo", null, null, null)).isNotEmpty()
                .allMatch(e -> e.getCategoria().equals("Torneo"));
        assertThat(servicio.buscar(null, "Valparaíso", null, null)).isNotEmpty()
                .allMatch(e -> e.getUbicacion().equals("Valparaíso"));
        assertThat(servicio.buscar("No existe", null, null, null)).isEmpty();

        LocalDate desde = LocalDate.of(2026, 12, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);
        assertThat(servicio.buscar("", "", desde, hasta))
                .allMatch(e -> !e.getFecha().isBefore(desde) && !e.getFecha().isAfter(hasta));
    }

    @Test
    void listasDeCategoriasYUbicacionesSinRepetir() {
        assertThat(servicio.categorias()).doesNotHaveDuplicates().isSorted().contains("Torneo");
        assertThat(servicio.ubicaciones()).doesNotHaveDuplicates().isSorted().contains("Santiago");
        assertThat(servicio.listarDestacados()).allMatch(Evento::isDestacado);
        assertThat(servicio.listarProximos(3)).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void creaActualizaYEliminaEventos() {
        Evento creado = servicio.crear(DatosPrueba.eventoRequest("  Evento con espacios  "));
        assertThat(creado.getTitulo()).isEqualTo("Evento con espacios");

        assertThat(servicio.actualizar(creado.getId(), DatosPrueba.eventoRequest("Actualizado")))
                .get().extracting(Evento::getTitulo).isEqualTo("Actualizado");
        assertThat(servicio.actualizar(999999L, DatosPrueba.eventoRequest("X"))).isEmpty();

        assertThat(servicio.eliminar(creado.getId())).isTrue();
        assertThat(servicio.eliminar(creado.getId())).isFalse();
        assertThat(servicio.buscarPorId(creado.getId())).isEmpty();
    }
}
