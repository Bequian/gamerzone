package com.bequian.gamerzone.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.api.dto.CompartirResponse;
import com.bequian.gamerzone.model.Compartido;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.CompartidoRepository;
import com.bequian.gamerzone.repository.EventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;

/**
 * Compartir eventos:
 *  - En GamerZone: el evento queda en el perfil del usuario y suma al contador del evento.
 *  - En redes sociales: enlaces oficiales de WhatsApp, X y Facebook. No se cargan scripts ni
 *    "widgets" de terceros, así esas redes no rastrean a los visitantes y la política CSP
 *    sigue permitiendo solo scripts propios.
 */
@Service
public class CompartirService {

    private final CompartidoRepository compartidoRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public CompartirService(CompartidoRepository compartidoRepository, EventoRepository eventoRepository,
                            UsuarioRepository usuarioRepository) {
        this.compartidoRepository = compartidoRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public CompartirResponse info(Evento evento, String urlEvento, String username) {
        String texto = "¡Mira este evento en GamerZone! " + evento.getTitulo();
        return new CompartirResponse(
                urlEvento,
                "https://wa.me/?text=" + codificar(texto + " " + urlEvento),
                "https://x.com/intent/post?text=" + codificar(texto) + "&url=" + codificar(urlEvento),
                "https://www.facebook.com/sharer/sharer.php?u=" + codificar(urlEvento),
                compartidoRepository.countByEventoId(evento.getId()),
                compartidoRepository.existsByEventoIdAndUsuarioUsername(evento.getId(), username));
    }

    // Comparte el evento en el perfil del usuario (si ya estaba compartido no hace nada)
    @Transactional
    public void compartir(Long eventoId, String username) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (!compartidoRepository.existsByEventoIdAndUsuarioUsername(eventoId, username)) {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
            compartidoRepository.save(new Compartido(evento, usuario));
        }
    }

    // Solo afecta al registro del propio usuario
    @Transactional
    public void dejarDeCompartir(Long eventoId, String username) {
        compartidoRepository.findByEventoIdAndUsuarioUsername(eventoId, username)
                .ifPresent(compartidoRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<Evento> compartidosPor(String username) {
        return compartidoRepository.findByUsuarioConEvento(username).stream()
                .map(Compartido::getEvento)
                .toList();
    }

    // Codifica el texto para usarlo en una URL (evita romper o manipular el enlace)
    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
