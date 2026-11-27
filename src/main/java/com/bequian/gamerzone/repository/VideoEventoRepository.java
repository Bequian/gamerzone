package com.bequian.gamerzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bequian.gamerzone.model.VideoEvento;
import com.bequian.gamerzone.model.Usuario;

public interface VideoEventoRepository extends JpaRepository<VideoEvento, Long> {

    List<VideoEvento> findByEventoIdOrderByIdAsc(Long eventoId);

    long countByEventoId(Long eventoId);

    Optional<VideoEvento> findByIdAndEventoId(Long id, Long eventoId);

    @Modifying
    @Query("delete from VideoEvento v where v.evento.id = :eventoId")
    void deleteByEvento(@Param("eventoId") Long eventoId);

    // Al eliminar un usuario, su multimedia queda a nombre del administrador que lo eliminó (no se pierde contenido)
    @Modifying
    @Query("update VideoEvento m set m.agregadoPor = :nuevo where m.agregadoPor.id = :usuarioId")
    void reasignarAutor(@Param("usuarioId") Long usuarioId, @Param("nuevo") Usuario nuevo);
}
