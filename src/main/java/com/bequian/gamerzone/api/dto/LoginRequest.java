package com.bequian.gamerzone.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Cuerpo JSON de POST /api/auth/login
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 100)
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72)
        String password) {
}