package com.bequian.gamerzone.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Compartido;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@Transactional
class CompartidoRepositoryTest {

    @Autowired
    private CompartidoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoService eventoService;

    @Test
    void cuentaBuscaYEliminaCompartidos() {
        Evento evento = eventoService.crear(DatosPrueba.eventoRequest("Evento repo compartidos"));
        Usuario user = usuarioRepository.findByUsername("user").orElseThrow();
        repository.save(new Compartido(evento, user));

        assertThat(repository.countByEventoId(evento.getId())).isEqualTo(1);
        assertThat(repository.existsByEventoIdAndUsuarioUsername(evento.getId(), "user")).isTrue();
        assertThat(repository.findByUsuarioConEvento("user")).extracting(Compartido::getEvento).contains(evento);
        assertThat(repository.findByEventoIdAndUsuarioUsername(evento.getId(), "user")).isPresent();

        repository.deleteByUsuario(user.getId());
        assertThat(repository.countByEventoId(evento.getId())).isZero();
    }
}
