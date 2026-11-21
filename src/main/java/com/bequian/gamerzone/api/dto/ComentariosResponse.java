package com.bequian.gamerzone.api.dto;

import java.util.List;

// Comentarios de un evento con su valoración promedio (null si aún no hay valoraciones)
public record ComentariosResponse(Double promedio, int total, List<ComentarioResponse> comentarios) {
}
