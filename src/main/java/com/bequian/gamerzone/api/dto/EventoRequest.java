package com.bequian.gamerzone.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.bequian.gamerzone.model.ServiciosEvento;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Cuerpo JSON para crear (POST) o editar (PUT) un evento. Todo se valida en el servidor.
public record EventoRequest(
        @NotBlank @Size(max = 100) String titulo,
        @NotBlank @Size(max = 40) String categoria,
        @NotBlank @Size(max = 1000) String descripcion,
        @NotNull @FutureOrPresent(message = "La fecha no puede estar en el pasado") LocalDate fecha,
        @NotNull LocalTime hora,
        @NotBlank @Size(max = 60) String ubicacion,
        @NotBlank @Size(max = 200) String direccion,
        @NotNull @Size(min = 1, max = 10, message = "Debe tener entre 1 y 10 organizadores")
        List<@NotBlank @Size(max = 100) String> organizadores,
        @NotNull @Size(max = 20, message = "Máximo 20 expositores")
        List<@NotBlank @Size(max = 100) String> expositores,
        @NotNull ServiciosEvento servicios,
        @NotNull(message = "Indica si el evento es destacado") Boolean destacado) {
}