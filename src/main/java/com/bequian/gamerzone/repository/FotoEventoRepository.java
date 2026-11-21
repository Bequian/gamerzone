package com.bequian.gamerzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bequian.gamerzone.model.FotoEvento;

public interface FotoEventoRepository extends JpaRepository<FotoEvento, Long> {

    // Solo los IDs: para listar las fotos no se cargan las imágenes completas en memoria
    @Query("select f.id from FotoEvento f where f.evento.id = :eventoId order by f.id")
    List<Long> findIdsByEvento(@Param("eventoId") Long eventoId);

    long countByEventoId(Long eventoId);

    Optional<FotoEvento> findByIdAndEventoId(Long id, Long eventoId);

    @Modifying
    @Query("delete from FotoEvento f where f.evento.id = :eventoId")
    void deleteByEvento(@Param("eventoId") Long eventoId);
}
