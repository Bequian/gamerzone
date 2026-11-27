package com.bequian.gamerzone.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.model.Usuario;

// @Transactional: cada test se revierte al terminar, así no deja datos para los demás
@SpringBootTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void buscaPorUsernameYCuentaPorRol() {
        long adminsAntes = repository.contarPorRol("ADMIN");
        repository.save(new Usuario("repo-admin@test.cl", "hash", "Repo", Set.of("USER", "ADMIN")));

        assertThat(repository.findByUsername("repo-admin@test.cl")).isPresent();
        assertThat(repository.existsByUsername("repo-admin@test.cl")).isTrue();
        assertThat(repository.existsByUsername("no-existe@test.cl")).isFalse();
        assertThat(repository.contarPorRol("ADMIN")).isEqualTo(adminsAntes + 1);
    }

    @Test
    void listaOrdenadaPorUsername() {
        assertThat(repository.findAllByOrderByUsernameAsc())
                .extracting(Usuario::getUsername)
                .isSorted()
                .contains("admin", "organizador", "user");
    }
}
