package com.bequian.gamerzone.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Comentario;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@Transactional
class ComentarioRepositoryTest {

    @Autowired
    private ComentarioRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoService eventoService;

    @Test
    void buscaPorEventoYEliminaPorUsuarioYPorEvento() {
        Evento evento = eventoService.crear(DatosPrueba.eventoRequest("Evento repo comentarios"));
        Usuario user = usuarioRepository.findByUsername("user").orElseThrow();
        Usuario org = usuarioRepository.findByUsername("organizador").orElseThrow();
        Comentario c = repository.save(new Comentario(evento, user, "Hola", 4));
        repository.save(new Comentario(evento, org, "Bien", 5));

        assertThat(repository.findByEventoConAutor(evento.getId())).hasSize(2);
        assertThat(repository.findByEventoIdAndUsuarioUsername(evento.getId(), "user")).isPresent();
        assertThat(repository.findByIdAndEventoConAutor(c.getId(), evento.getId())).isPresent();

        repository.deleteByUsuario(user.getId());
        assertThat(repository.findByEventoConAutor(evento.getId())).hasSize(1);

        repository.deleteByEvento(evento.getId());
        assertThat(repository.findByEventoConAutor(evento.getId())).isEmpty();
    }
}
