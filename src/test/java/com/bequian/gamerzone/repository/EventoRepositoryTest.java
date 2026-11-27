package com.bequian.gamerzone.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@Transactional
class EventoRepositoryTest {

    @Autowired
    private EventoRepository repository;

    @Autowired
    private EventoService eventoService;

    @Test
    void guardaYBuscaEventos() {
        long antes = repository.count();
        Evento guardado = eventoService.crear(DatosPrueba.eventoRequest("Evento repositorio"));

        assertThat(repository.count()).isEqualTo(antes + 1);
        assertThat(repository.findById(guardado.getId())).get()
                .extracting(Evento::getTitulo).isEqualTo("Evento repositorio");
    }
}
