package com.bequian.gamerzone.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bequian.gamerzone.model.Evento;

@Service
public class EventoService {

    // Más adelante esto vendrá de una base de datos
    private final List<Evento> eventos = List.of(
        new Evento("Torneo Valorant 5v5", "Torneo", "15 Nov 2026",
            "Arma tu equipo y compite por el primer lugar en nuestro torneo mensual."),
        new Evento("LAN Party Santiago", "Presencial", "22 Nov 2026",
            "Trae tu equipo y juega toda la noche con la comunidad GamerZone."),
        new Evento("Speedrun Challenge", "Online", "30 Nov 2026",
            "¿Eres el más rápido? Demuéstralo en nuestro desafío de speedrun.")
    );

    public List<Evento> listarTodos() {
        return eventos;
    }
}