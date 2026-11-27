package com.bequian.gamerzone.api.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Cuerpo JSON para crear un usuario desde la API de administración
public record UsuarioRequest(
        @NotBlank @Size(max = 60) String nombre,
        @NotBlank @Email @Size(max = 100) String correo,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "La contraseña debe incluir al menos una letra y un número")
        String password,
        @NotNull(message = "Indica los roles") List<String> roles) {
}
