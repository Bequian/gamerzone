package com.bequian.gamerzone.api.dto;

import java.time.LocalDateTime;

// Comentario tal como se muestra. Se expone el NOMBRE del autor, nunca su correo (evita exponer datos personales).
// "propio" indica si el comentario es del usuario que hace la consulta (para mostrarle el botón eliminar).
public record ComentarioResponse(Long id, String autor, String texto, int valoracion,
                                 LocalDateTime fecha, boolean propio) {
}
