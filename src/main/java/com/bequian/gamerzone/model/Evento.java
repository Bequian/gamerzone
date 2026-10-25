package com.bequian.gamerzone.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record Evento(
        Long id,
        String titulo,
        String categoria,
        String descripcion,
        LocalDate fecha,
        LocalTime hora,
        String ubicacion,
        String direccion,
        List<String> organizadores,
        ServiciosEvento servicios,
        List<String> expositores,
        boolean destacado) {
}