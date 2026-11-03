package com.bequian.gamerzone.api;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.api.dto.EventoRequest;
import com.bequian.gamerzone.api.dto.EventoResponse;
import com.bequian.gamerzone.service.EventoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/eventos")
public class EventoApiController {

    private final EventoService eventoService;

    public EventoApiController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // [PÚBLICA] Listado y búsqueda de eventos (todos los filtros son opcionales)
    @GetMapping
    public List<EventoResponse> listar(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "ubicacion", required = false) String ubicacion,
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return eventoService.buscar(categoria, ubicacion, desde, hasta).stream()
                .map(EventoResponse::desde)
                .toList();
    }

    // [PRIVADA] Detalle completo de un evento (requiere token)
    @GetMapping("/{id}")
    public EventoResponse detalle(@PathVariable("id") Long id) {
        return eventoService.buscarPorId(id)
                .map(EventoResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    // [PRIVADA - ORGANIZADOR/ADMIN] Crear evento
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody EventoRequest request) {
        EventoResponse creado = EventoResponse.desde(eventoService.crear(request));
        return ResponseEntity.created(URI.create("/api/eventos/" + creado.id())).body(creado);
    }

    // [PRIVADA - ORGANIZADOR/ADMIN] Editar evento
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public EventoResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody EventoRequest request) {
        return eventoService.actualizar(id, request)
                .map(EventoResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    // [PRIVADA - ORGANIZADOR/ADMIN] Eliminar evento
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        if (!eventoService.eliminar(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }
        return ResponseEntity.noContent().build();
    }
}