package com.bequian.gamerzone.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bequian.gamerzone.model.Evento;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Evento> eventos = List.of(
            new Evento("Torneo Valorant 5v5", "Torneo", "15 Nov 2026",
                "Arma tu equipo y compite por el primer lugar en nuestro torneo mensual."),
            new Evento("LAN Party Santiago", "Presencial", "22 Nov 2026",
                "Trae tu equipo y juega toda la noche con la comunidad GamerZone."),
            new Evento("Speedrun Challenge", "Online", "30 Nov 2026",
                "¿Eres el más rápido? Demuéstralo en nuestro desafío de speedrun.")
        );
        model.addAttribute("eventos", eventos);
        return "home";
    }
}