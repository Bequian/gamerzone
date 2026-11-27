package com.bequian.gamerzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bequian.gamerzone.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // "join fetch" trae el autor en la misma consulta (consulta parametrizada: sin inyección SQL)
    @Query("select c from Comentario c join fetch c.usuario where c.evento.id = :eventoId order by c.fecha desc")
    List<Comentario> findByEventoConAutor(@Param("eventoId") Long eventoId);

    Optional<Comentario> findByEventoIdAndUsuarioUsername(Long eventoId, String username);

    @Query("select c from Comentario c join fetch c.usuario where c.id = :id and c.evento.id = :eventoId")
    Optional<Comentario> findByIdAndEventoConAutor(@Param("id") Long id, @Param("eventoId") Long eventoId);

    @Modifying
    @Query("delete from Comentario c where c.evento.id = :eventoId")
    void deleteByEvento(@Param("eventoId") Long eventoId);

    // Al eliminar un usuario se eliminan también sus comentarios
    @Modifying
    @Query("delete from Comentario c where c.usuario.id = :usuarioId")
    void deleteByUsuario(@Param("usuarioId") Long usuarioId);
}
