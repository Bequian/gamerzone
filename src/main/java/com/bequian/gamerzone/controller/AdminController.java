package com.bequian.gamerzone.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.service.EventoService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")   // Segunda capa de seguridad
public class AdminController {

    private final EventoService eventoService;

    public AdminController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public String panel(Model model) {
        List<Evento> eventos = eventoService.listarTodos();
        model.addAttribute("eventos", eventos);
        model.addAttribute("totalEventos", eventos.size());
        return "admin/panel";
    }
}