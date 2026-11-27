package com.bequian.gamerzone.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.VideoEvento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@Transactional
class VideoEventoRepositoryTest {

    @Autowired
    private VideoEventoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoService eventoService;

    @Test
    void buscaReasignaAutorYEliminaPorEvento() {
        Evento evento = eventoService.crear(DatosPrueba.eventoRequest("Evento repo Video"));
        Usuario org = usuarioRepository.findByUsername("organizador").orElseThrow();
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        VideoEvento guardado = repository.save(new VideoEvento(evento, org, "dQw4w9WgXcQ", "Tráiler"));

        assertThat(repository.findByEventoIdOrderByIdAsc(evento.getId())).hasSize(1);
        assertThat(repository.countByEventoId(evento.getId())).isEqualTo(1);
        assertThat(repository.findByIdAndEventoId(guardado.getId(), evento.getId())).isPresent();
        assertThat(repository.findByIdAndEventoId(guardado.getId(), 999999L)).isEmpty();

        repository.reasignarAutor(org.getId(), admin);   // no falla y conserva el registro
        assertThat(repository.countByEventoId(evento.getId())).isEqualTo(1);

        repository.deleteByEvento(evento.getId());
        assertThat(repository.countByEventoId(evento.getId())).isZero();
    }
}
