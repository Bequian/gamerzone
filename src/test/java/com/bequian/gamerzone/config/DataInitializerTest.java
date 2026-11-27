package com.bequian.gamerzone.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.EventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;

@SpringBootTest
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void creaLosUsuariosBaseConContrasenaCifradaYSusRoles() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(admin.getPassword()).isNotEqualTo("test-admin");   // nunca en texto plano
        assertThat(passwordEncoder.matches("test-admin", admin.getPassword())).isTrue();
        assertThat(usuarioRepository.findByUsername("organizador").orElseThrow().getRoles()).contains("ORGANIZADOR");
    }

    @Test
    void ejecutarloDeNuevoNoDuplicaUsuariosNiEventos() {
        long usuarios = usuarioRepository.count();
        long eventos = eventoRepository.count();

        dataInitializer.run();

        assertThat(usuarioRepository.count()).isEqualTo(usuarios);
        assertThat(eventoRepository.count()).isEqualTo(eventos);
        assertThat(passwordEncoder.matches("test-user",
                usuarioRepository.findByUsername("user").orElseThrow().getPassword())).isTrue();
    }
}
