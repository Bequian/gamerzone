package com.bequian.gamerzone.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bequian.gamerzone.api.dto.ComentariosResponse;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.service.ComentarioService;
import com.bequian.gamerzone.service.CompartirService;
import com.bequian.gamerzone.service.EventoService;
import com.bequian.gamerzone.service.MultimediaService;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final ComentarioService comentarioService;
    private final MultimediaService multimediaService;
    private final CompartirService compartirService;

    public EventoController(EventoService eventoService, ComentarioService comentarioService,
                            MultimediaService multimediaService, CompartirService compartirService) {
        this.eventoService = eventoService;
        this.comentarioService = comentarioService;
        this.multimediaService = multimediaService;
        this.compartirService = compartirService;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "ubicacion", required = false) String ubicacion,
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            model.addAttribute("errorFechas", "La fecha \"desde\" no puede ser posterior a la fecha \"hasta\".");
        }

        model.addAttribute("eventos", eventoService.buscar(categoria, ubicacion, desde, hasta));
        model.addAttribute("categorias", eventoService.categorias());
        model.addAttribute("ubicaciones", eventoService.ubicaciones());

        // Se devuelven los filtros para que el formulario los mantenga seleccionados
        model.addAttribute("categoriaSel", categoria);
        model.addAttribute("ubicacionSel", ubicacion);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "eventos/lista";
    }

    // [PRIVADA] Detalle del evento con fotos, videos, comentarios y opciones para compartir
    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Long id, Authentication auth, Model model) {
        Evento evento = eventoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String username = auth.getName();

        ComentariosResponse comentarios = comentarioService.listar(id, username);
        String urlEvento = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/eventos/{id}").buildAndExpand(id).toUriString();

        model.addAttribute("evento", evento);
        model.addAttribute("comentarios", comentarios);
        model.addAttribute("estrellasPromedio",
                comentarios.promedio() == null ? 0 : (int) Math.round(comentarios.promedio()));
        // Comentario del propio usuario (si ya comentó, el formulario aparece con sus datos para editarlo)
        model.addAttribute("miComentario", comentarios.comentarios().stream()
                .filter(c -> c.propio()).findFirst().orElse(null));
        model.addAttribute("fotos", multimediaService.idsFotos(id));
        model.addAttribute("videos", multimediaService.videos(id));
        model.addAttribute("maxFotos", MultimediaService.MAX_FOTOS_POR_EVENTO);
        model.addAttribute("maxVideos", MultimediaService.MAX_VIDEOS_POR_EVENTO);
        model.addAttribute("compartir", compartirService.info(evento, urlEvento, username));
        return "eventos/detalle";
    }
}
