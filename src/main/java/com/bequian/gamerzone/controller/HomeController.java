package com.bequian.gamerzone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bequian.gamerzone.service.EventoService;

@Controller
public class HomeController {

    private final EventoService eventoService;

    // Inyección por constructor
    public HomeController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("eventos", eventoService.listarTodos());
        return "home";
    }
}