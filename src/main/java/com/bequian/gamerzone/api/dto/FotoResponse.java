package com.bequian.gamerzone.api.dto;

// Foto de un evento: la imagen se descarga desde "url" (también requiere token)
public record FotoResponse(Long id, String url) {
}
