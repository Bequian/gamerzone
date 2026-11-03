package com.bequian.gamerzone.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.ServiciosEvento;

// Lo que la API devuelve de un evento (no se expone la entidad JPA directamente)
public record EventoResponse(
        Long id,
        String titulo,
        String categoria,
        String descripcion,
        LocalDate fecha,
        LocalTime hora,
        String ubicacion,
        String direccion,
        List<String> organizadores,
        List<String> expositores,
        ServiciosEvento servicios,
        boolean destacado) {

    public static EventoResponse desde(Evento e) {
        return new EventoResponse(e.getId(), e.getTitulo(), e.getCategoria(), e.getDescripcion(),
                e.getFecha(), e.getHora(), e.getUbicacion(), e.getDireccion(),
                List.copyOf(e.getOrganizadores()), List.copyOf(e.getExpositores()),
                e.getServicios(), e.isDestacado());
    }
}