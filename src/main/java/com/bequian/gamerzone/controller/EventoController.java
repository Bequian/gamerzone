package com.bequian.gamerzone.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.service.EventoService;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
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

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Long id, Model model) {
        Evento evento = eventoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("evento", evento);
        return "eventos/detalle";
    }
}