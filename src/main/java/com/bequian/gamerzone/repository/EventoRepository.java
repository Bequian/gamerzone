package com.bequian.gamerzone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bequian.gamerzone.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    // SELECT * FROM evento ORDER BY fecha, hora
    List<Evento> findAllByOrderByFechaAscHoraAsc();

    // SELECT * FROM evento WHERE destacado = true ORDER BY fecha, hora
    List<Evento> findByDestacadoTrueOrderByFechaAscHoraAsc();
}