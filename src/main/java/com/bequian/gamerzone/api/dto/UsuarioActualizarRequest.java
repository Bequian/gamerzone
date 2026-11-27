package com.bequian.gamerzone.api.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Cuerpo JSON para editar un usuario. La contraseña es opcional: si no viene, no se cambia.
public record UsuarioActualizarRequest(
        @NotBlank @Size(max = 60) String nombre,
        @NotNull(message = "Indica los roles") List<String> roles,
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "La contraseña debe incluir al menos una letra y un número")
        String password) {
}
