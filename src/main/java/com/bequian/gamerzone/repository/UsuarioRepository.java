package com.bequian.gamerzone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bequian.gamerzone.model.Usuario;

// Spring Data genera la implementación (y el SQL) a partir del nombre de cada método
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}