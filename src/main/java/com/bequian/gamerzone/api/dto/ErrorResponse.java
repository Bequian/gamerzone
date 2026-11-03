package com.bequian.gamerzone.api.dto;

import java.util.Map;

// Formato común para los errores de la API
public record ErrorResponse(int status, String mensaje, Map<String, String> errores) {

    public ErrorResponse(int status, String mensaje) {
        this(status, mensaje, Map.of());
    }
}