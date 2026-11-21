package com.bequian.gamerzone.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bequian.gamerzone.api.dto.ComentarioRequest;
import com.bequian.gamerzone.api.dto.ComentarioResponse;
import com.bequian.gamerzone.api.dto.ComentariosResponse;
import com.bequian.gamerzone.service.ComentarioService;

import jakarta.validation.Valid;

// [PRIVADA] Comentarios y valoraciones de un evento (requiere token JWT)
@RestController
@RequestMapping("/api/eventos/{id}/comentarios")
public class ComentarioApiController {

    private final ComentarioService comentarioService;

    public ComentarioApiController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ComentariosResponse listar(@PathVariable("id") Long id, Authentication auth) {
        return comentarioService.listar(id, auth.getName());
    }

    // 201 si es un comentario nuevo, 200 si actualizó el comentario que el usuario ya tenía
    @PostMapping
    public ResponseEntity<ComentarioResponse> comentar(@PathVariable("id") Long id,
                                                       @Valid @RequestBody ComentarioRequest request,
                                                       Authentication auth) {
        ComentarioService.Guardado guardado = comentarioService.guardar(id, auth.getName(), request);
        if (guardado.creado()) {
            return ResponseEntity
                    .created(URI.create("/api/eventos/" + id + "/comentarios/" + guardado.comentario().id()))
                    .body(guardado.comentario());
        }
        return ResponseEntity.ok(guardado.comentario());
    }

    // Solo el autor del comentario o un ADMIN (403 en otro caso)
    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id,
                                         @PathVariable("comentarioId") Long comentarioId,
                                         Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        comentarioService.eliminar(id, comentarioId, auth.getName(), esAdmin);
        return ResponseEntity.noContent().build();
    }
}
