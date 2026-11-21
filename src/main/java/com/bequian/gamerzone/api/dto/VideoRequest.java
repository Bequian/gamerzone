package com.bequian.gamerzone.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos para agregar un video de YouTube a un evento
public record VideoRequest(

        @NotBlank(message = "Ingresa el enlace del video de YouTube")
        @Size(max = 300, message = "El enlace es demasiado largo")
        String url,

        @Size(max = 100, message = "El título no puede superar los 100 caracteres")
        String titulo) {
}
