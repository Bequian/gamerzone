package com.bequian.gamerzone.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.api.dto.ComentarioRequest;
import com.bequian.gamerzone.api.dto.ComentarioResponse;
import com.bequian.gamerzone.api.dto.ComentariosResponse;
import com.bequian.gamerzone.model.Comentario;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.ComentarioRepository;
import com.bequian.gamerzone.repository.EventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;

@Service
public class ComentarioService {

    // Resultado de guardar: indica si el comentario es nuevo o si se actualizó uno existente
    public record Guardado(boolean creado, ComentarioResponse comentario) {
    }

    private final ComentarioRepository comentarioRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             EventoRepository eventoRepository,
                             UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public ComentariosResponse listar(Long eventoId, String usernameActual) {
        verificarEvento(eventoId);
        List<ComentarioResponse> comentarios = comentarioRepository.findByEventoConAutor(eventoId).stream()
                .map(c -> aResponse(c, usernameActual))
                .toList();

        Double promedio = comentarios.isEmpty() ? null
                : Math.round(comentarios.stream().mapToInt(ComentarioResponse::valoracion).average().orElse(0) * 10) / 10.0;
        return new ComentariosResponse(promedio, comentarios.size(), comentarios);
    }

    /**
     * Crea el comentario del usuario o, si ya había comentado este evento, lo actualiza.
     * Así cada usuario tiene una sola valoración por evento.
     */
    @Transactional
    public Guardado guardar(Long eventoId, String username, ComentarioRequest request) {
        Evento evento = verificarEvento(eventoId);
        String texto = request.texto().trim();
        int valoracion = request.valoracion();

        return comentarioRepository.findByEventoIdAndUsuarioUsername(eventoId, username)
                .map(existente -> {
                    existente.actualizar(texto, valoracion);
                    return new Guardado(false, aResponse(existente, username));
                })
                .orElseGet(() -> {
                    Usuario usuario = buscarUsuario(username);
                    Comentario nuevo = comentarioRepository.save(new Comentario(evento, usuario, texto, valoracion));
                    return new Guardado(true, aResponse(nuevo, username));
                });
    }

    /**
     * Elimina un comentario. Solo puede hacerlo su autor o un ADMIN (control de acceso a nivel
     * de objeto: no basta con estar autenticado, OWASP A01).
     */
    @Transactional
    public void eliminar(Long eventoId, Long comentarioId, String username, boolean esAdmin) {
        Comentario comentario = comentarioRepository.findByIdAndEventoConAutor(comentarioId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        if (!esAdmin && !comentario.getUsuario().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes eliminar tus propios comentarios");
        }
        comentarioRepository.delete(comentario);
    }

    private ComentarioResponse aResponse(Comentario c, String usernameActual) {
        Usuario autor = c.getUsuario();
        return new ComentarioResponse(c.getId(), autor.getNombre(), c.getTexto(), c.getValoracion(),
                c.getFecha(), autor.getUsername().equals(usernameActual));
    }

    private Evento verificarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }
}
