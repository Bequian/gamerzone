package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.api.dto.CompartirResponse;
import com.bequian.gamerzone.model.Evento;

@SpringBootTest
class CompartirServiceTest {

    @Autowired
    private CompartirService servicio;

    @Autowired
    private EventoService eventoService;

    @Test
    void generaEnlacesCodificadosParaRedesSociales() {
        Evento evento = eventoService.crear(DatosPrueba.eventoRequest("Torneo & Amigos"));
        CompartirResponse r = servicio.info(evento, "https://gz.cl/eventos/" + evento.getId(), "user");

        assertThat(r.whatsapp()).startsWith("https://wa.me/?text=").contains("Torneo%20%26%20Amigos");
        assertThat(r.x()).contains("&url=https%3A%2F%2Fgz.cl");
        assertThat(r.facebook()).startsWith("https://www.facebook.com/sharer/sharer.php?u=");
        assertThat(r.compartidoPorMi()).isFalse();
    }

    @Test
    void compartirDosVecesNoDuplicaYSePuedeQuitar() {
        Evento evento = eventoService.crear(DatosPrueba.eventoRequest("Evento servicio compartir"));
        servicio.compartir(evento.getId(), "user");
        servicio.compartir(evento.getId(), "user");

        assertThat(servicio.info(evento, "u", "user").totalCompartidos()).isEqualTo(1);
        assertThat(servicio.compartidosPor("user")).extracting(Evento::getId).contains(evento.getId());

        servicio.dejarDeCompartir(evento.getId(), "user");
        assertThat(servicio.info(evento, "u", "user").compartidoPorMi()).isFalse();
    }

    @Test
    void eventoOUsuarioInexistentesSeRechazan() {
        long id = eventoService.crear(DatosPrueba.eventoRequest("Evento compartir errores")).getId();
        assertThatThrownBy(() -> servicio.compartir(999999L, "user")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> servicio.compartir(id, "fantasma")).isInstanceOf(ResponseStatusException.class);
    }
}
