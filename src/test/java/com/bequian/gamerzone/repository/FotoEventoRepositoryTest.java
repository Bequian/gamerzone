package com.bequian.gamerzone.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.FotoEvento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@Transactional
class FotoEventoRepositoryTest {

    @Autowired
    private FotoEventoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoService eventoService;

    @Test
    void buscaReasignaAutorYEliminaPorEvento() {
        Evento evento = eventoService.crear(DatosPrueba.eventoRequest("Evento repo Foto"));
        Usuario org = usuarioRepository.findByUsername("organizador").orElseThrow();
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        FotoEvento guardado = repository.save(new FotoEvento(evento, org, "image/png", new byte[] {1, 2, 3}));

        assertThat(repository.findIdsByEvento(evento.getId())).hasSize(1);
        assertThat(repository.countByEventoId(evento.getId())).isEqualTo(1);
        assertThat(repository.findByIdAndEventoId(guardado.getId(), evento.getId())).isPresent();
        assertThat(repository.findByIdAndEventoId(guardado.getId(), 999999L)).isEmpty();

        repository.reasignarAutor(org.getId(), admin);   // no falla y conserva el registro
        assertThat(repository.countByEventoId(evento.getId())).isEqualTo(1);

        repository.deleteByEvento(evento.getId());
        assertThat(repository.countByEventoId(evento.getId())).isZero();
    }
}
