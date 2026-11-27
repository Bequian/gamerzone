package com.bequian.gamerzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bequian.gamerzone.model.Usuario;

// Spring Data genera la implementación (y el SQL) a partir del nombre de cada método
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Usuario> findAllByOrderByUsernameAsc();

    // Cuántos usuarios tienen un rol (se usa para no dejar el sitio sin administradores)
    @Query("select count(u) from Usuario u join u.roles r where r = :rol")
    long contarPorRol(@Param("rol") String rol);
}