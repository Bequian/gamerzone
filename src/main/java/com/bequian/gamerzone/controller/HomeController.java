package com.bequian.gamerzone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bequian.gamerzone.service.EventoService;
import com.bequian.gamerzone.service.MarcaService;

@Controller
public class HomeController {

    private final EventoService eventoService;
    private final MarcaService marcaService;

    public HomeController(EventoService eventoService, MarcaService marcaService) {
        this.eventoService = eventoService;
        this.marcaService = marcaService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("destacados", eventoService.listarDestacados());
        model.addAttribute("proximos", eventoService.listarProximos(6));
        model.addAttribute("marcas", marcaService.listarTodas());
        return "home";
    }
}