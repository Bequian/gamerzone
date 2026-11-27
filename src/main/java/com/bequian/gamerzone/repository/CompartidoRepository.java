package com.bequian.gamerzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bequian.gamerzone.model.Compartido;

public interface CompartidoRepository extends JpaRepository<Compartido, Long> {

    long countByEventoId(Long eventoId);

    boolean existsByEventoIdAndUsuarioUsername(Long eventoId, String username);

    @Query("select c from Compartido c join fetch c.evento where c.usuario.username = :username order by c.fecha desc")
    List<Compartido> findByUsuarioConEvento(@Param("username") String username);

    Optional<Compartido> findByEventoIdAndUsuarioUsername(Long eventoId, String username);

    @Modifying
    @Query("delete from Compartido c where c.evento.id = :eventoId")
    void deleteByEvento(@Param("eventoId") Long eventoId);

    // Al eliminar un usuario se eliminan también sus eventos compartidos
    @Modifying
    @Query("delete from Compartido c where c.usuario.id = :usuarioId")
    void deleteByUsuario(@Param("usuarioId") Long usuarioId);
}
