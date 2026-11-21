package com.bequian.gamerzone.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bequian.gamerzone.api.dto.CompartirResponse;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.service.CompartirService;
import com.bequian.gamerzone.service.EventoService;

// [PRIVADA] Compartir un evento: enlaces para redes sociales y registro de "compartido" en GamerZone
@RestController
@RequestMapping("/api/eventos/{id}/compartir")
public class CompartirApiController {

    private final CompartirService compartirService;
    private final EventoService eventoService;

    public CompartirApiController(CompartirService compartirService, EventoService eventoService) {
        this.compartirService = compartirService;
        this.eventoService = eventoService;
    }

    @GetMapping
    public CompartirResponse enlaces(@PathVariable("id") Long id, Authentication auth) {
        return respuesta(id, auth.getName());
    }

    @PostMapping
    public CompartirResponse compartir(@PathVariable("id") Long id, Authentication auth) {
        compartirService.compartir(id, auth.getName());
        return respuesta(id, auth.getName());
    }

    @DeleteMapping
    public CompartirResponse dejarDeCompartir(@PathVariable("id") Long id, Authentication auth) {
        compartirService.dejarDeCompartir(id, auth.getName());
        return respuesta(id, auth.getName());
    }

    private CompartirResponse respuesta(Long id, String username) {
        Evento evento = eventoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        // Enlace al detalle en el sitio web (no a la API), que es lo que se comparte en redes
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/eventos/{id}").buildAndExpand(id).toUriString();
        return compartirService.info(evento, url, username);
    }
}
