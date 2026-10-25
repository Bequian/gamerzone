package com.bequian.gamerzone.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.ServiciosEvento;

@Service
public class EventoService {

    // Simulacion de base de datos con eventos precargados.
    // Se ordenan una sola vez por fecha y hora.
    private final List<Evento> eventos = List.of(
        new Evento(1L, "Torneo Valorant 5v5", "Torneo",
            "Arma tu equipo y compite por el primer lugar en nuestro torneo mensual. Fase de grupos y playoffs al mejor de tres.",
            LocalDate.of(2026, 11, 15), LocalTime.of(15, 0),
            "Santiago", "Av. Providencia 1234, Providencia",
            List.of("GamerZone", "Liga Andina de Esports"),
            new ServiciosEvento(true, true, true, false),
            List.of("Equipo Andes Esports", "Caster Sofía \"Nova\" Rojas"),
            true),
        new Evento(2L, "LAN Party Santiago", "LAN Party",
            "Trae tu equipo y juega toda la noche con la comunidad GamerZone. Mesas, energía y red cableada para 200 jugadores.",
            LocalDate.of(2026, 11, 22), LocalTime.of(18, 0),
            "Santiago", "Centro de Eventos Matucana 456, Estación Central",
            List.of("GamerZone", "Club LAN Chile"),
            new ServiciosEvento(true, true, true, false),
            List.of("Técnicos de HexaGear", "Streamer Diego \"Pixel\" Fuentes"),
            true),
        new Evento(3L, "Speedrun Challenge", "Online",
            "¿Eres el más rápido? Demuéstralo en nuestro desafío de speedrun transmitido en vivo.",
            LocalDate.of(2026, 11, 30), LocalTime.of(20, 0),
            "Online", "Transmisión en vivo desde el canal oficial de GamerZone",
            List.of("GamerZone"),
            new ServiciosEvento(false, false, false, false),
            List.of("Comunidad Speedrun Chile"),
            false),
        new Evento(4L, "Feria Retro Gaming", "Feria",
            "Consolas clásicas, arcades, coleccionables y zona de juego libre para toda la familia.",
            LocalDate.of(2026, 12, 5), LocalTime.of(11, 0),
            "Valparaíso", "Av. Brasil 2950, Valparaíso",
            List.of("Asociación Retro Porteña", "GamerZone"),
            new ServiciosEvento(true, true, true, true),
            List.of("Coleccionistas de la V Región", "Museo del Videojuego (muestra itinerante)"),
            true),
        new Evento(5L, "Torneo FC Clausura", "Torneo",
            "Torneo 1 vs 1 de fútbol virtual con premios para los tres primeros lugares.",
            LocalDate.of(2026, 12, 12), LocalTime.of(16, 0),
            "Concepción", "Calle O'Higgins 780, Concepción",
            List.of("Liga Sur Gaming"),
            new ServiciosEvento(false, true, true, true),
            List.of("Jugador profesional Matías Soto"),
            false),
        new Evento(6L, "Encuentro Indie Devs", "Charla",
            "Desarrolladores independientes muestran sus juegos y cuentan cómo los crearon. Incluye ronda de preguntas.",
            LocalDate.of(2026, 12, 19), LocalTime.of(10, 30),
            "Santiago", "Av. Libertador Bernardo O'Higgins 3363, Estación Central",
            List.of("GamerZone", "Colectivo Indie Chile"),
            new ServiciosEvento(true, true, false, false),
            List.of("Estudio Lumen Games", "Estudio Pájaro Azul"),
            false)
    ).stream()
     .sorted(Comparator.comparing(Evento::fecha).thenComparing(Evento::hora))
     .toList();

    public List<Evento> listarTodos() {
        return eventos;
    }

    // Muestra eventos que no han pasado, limitando la cantidad a mostrar
    public List<Evento> listarProximos(int cantidad) {
        LocalDate hoy = LocalDate.now();
        return eventos.stream()
                .filter(e -> !e.fecha().isBefore(hoy))
                .limit(cantidad)
                .toList();
    }

    public List<Evento> listarDestacados() {
        return eventos.stream().filter(Evento::destacado).toList();
    }

    public Optional<Evento> buscarPorId(Long id) {
        return eventos.stream().filter(e -> e.id().equals(id)).findFirst();
    }

    // Filtro opcional por categoria, ubicacion y rango de fechas. Si no se entrega un filtro, se ignora.
    public List<Evento> buscar(String categoria, String ubicacion, LocalDate desde, LocalDate hasta) {
        return eventos.stream()
                .filter(e -> !StringUtils.hasText(categoria) || e.categoria().equals(categoria))
                .filter(e -> !StringUtils.hasText(ubicacion) || e.ubicacion().equals(ubicacion))
                .filter(e -> desde == null || !e.fecha().isBefore(desde))
                .filter(e -> hasta == null || !e.fecha().isAfter(hasta))
                .toList();
    }

    public List<String> categorias() {
        return eventos.stream().map(Evento::categoria).distinct().sorted().toList();
    }

    public List<String> ubicaciones() {
        return eventos.stream().map(Evento::ubicacion).distinct().sorted().toList();
    }
}