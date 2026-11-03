package com.bequian.gamerzone.api.dto;

import java.util.List;

// Respuesta de un login exitoso
public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String username,
        List<String> roles) {
}