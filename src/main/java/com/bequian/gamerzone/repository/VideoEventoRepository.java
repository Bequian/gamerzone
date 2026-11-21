package com.bequian.gamerzone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bequian.gamerzone.model.VideoEvento;

public interface VideoEventoRepository extends JpaRepository<VideoEvento, Long> {

    List<VideoEvento> findByEventoIdOrderByIdAsc(Long eventoId);

    long countByEventoId(Long eventoId);

    Optional<VideoEvento> findByIdAndEventoId(Long id, Long eventoId);

    @Modifying
    @Query("delete from VideoEvento v where v.evento.id = :eventoId")
    void deleteByEvento(@Param("eventoId") Long eventoId);
}
