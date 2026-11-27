package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.api.dto.ComentarioRequest;
import com.bequian.gamerzone.api.dto.ComentariosResponse;

@SpringBootTest
class ComentarioServiceTest {

    @Autowired
    private ComentarioService servicio;

    @Autowired
    private EventoService eventoService;

    private long nuevoEvento() {
        return eventoService.crear(DatosPrueba.eventoRequest("Evento servicio comentarios")).getId();
    }

    @Test
    void calculaElPromedioYMarcaLosComentariosPropios() {
        long id = nuevoEvento();
        assertThat(servicio.listar(id, "user").promedio()).isNull();

        assertThat(servicio.guardar(id, "user", new ComentarioRequest("  Bueno  ", 4)).creado()).isTrue();
        servicio.guardar(id, "organizador", new ComentarioRequest("Regular", 3));

        ComentariosResponse r = servicio.listar(id, "user");
        assertThat(r.total()).isEqualTo(2);
        assertThat(r.promedio()).isEqualTo(3.5);
        assertThat(r.comentarios()).filteredOn(c -> c.propio()).singleElement()
                .satisfies(c -> assertThat(c.texto()).isEqualTo("Bueno"));   // se guarda sin espacios
        // Se muestra el nombre del autor, nunca su correo/usuario
        assertThat(r.comentarios()).extracting(c -> c.autor()).doesNotContain("user", "organizador");
    }

    @Test
    void soloElAutorOUnAdminPuedenEliminar() {
        long id = nuevoEvento();
        Long comentarioId = servicio.guardar(id, "user", new ComentarioRequest("Hola", 5)).comentario().id();

        assertThatThrownBy(() -> servicio.eliminar(id, comentarioId, "organizador", false))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("propios");
        servicio.eliminar(id, comentarioId, "admin", true);
        assertThat(servicio.listar(id, "user").total()).isZero();
    }

    @Test
    void eventoComentarioOUsuarioInexistentesSeRechazan() {
        long id = nuevoEvento();
        assertThatThrownBy(() -> servicio.listar(999999L, "user")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> servicio.eliminar(id, 999999L, "admin", true))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> servicio.guardar(id, "fantasma", new ComentarioRequest("Hola", 5)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
