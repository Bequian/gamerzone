package com.bequian.gamerzone.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Datos para comentar y valorar un evento (se usa en el formulario web y en la API)
public record ComentarioRequest(

        @NotBlank(message = "El comentario no puede estar vacío")
        @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
        String texto,

        @NotNull(message = "Selecciona una valoración")
        @Min(value = 1, message = "La valoración mínima es 1")
        @Max(value = 5, message = "La valoración máxima es 5")
        Integer valoracion) {
}
