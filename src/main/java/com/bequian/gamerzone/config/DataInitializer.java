package com.bequian.gamerzone.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.ServiciosEvento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.EventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;

/**
 * Se ejecuta una vez al arrancar la aplicación:
 * - Crea (o actualiza) los 3 usuarios base con las contraseñas de las variables de entorno.
 * - Si la tabla de eventos está vacía, carga los eventos de ejemplo.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gamerzone.user.password}")
    private String userPassword;

    @Value("${gamerzone.admin.password}")
    private String adminPassword;

    @Value("${gamerzone.organizador.password}")
    private String organizadorPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EventoRepository eventoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        crearOActualizarUsuario("user", userPassword, "Usuario de prueba", Set.of("USER"));
        crearOActualizarUsuario("admin", adminPassword, "Administrador", Set.of("USER", "ADMIN"));
        crearOActualizarUsuario("organizador", organizadorPassword, "Organizador", Set.of("USER", "ORGANIZADOR"));

        if (eventoRepository.count() == 0) {
            eventoRepository.saveAll(eventosDeEjemplo());
        }
    }

    // Si el usuario ya existe, se actualiza su contraseña: la variable de entorno siempre manda
    private void crearOActualizarUsuario(String username, String password, String nombre, Set<String> roles) {
        String hash = passwordEncoder.encode(password);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseGet(() -> new Usuario(username, hash, nombre, roles));
        usuario.setPassword(hash);
        usuario.setRoles(roles);
        usuarioRepository.save(usuario);
    }

    private List<Evento> eventosDeEjemplo() {
        return List.of(
            new Evento("Torneo Valorant 5v5", "Torneo",
                "Arma tu equipo y compite por el primer lugar en nuestro torneo mensual. Fase de grupos y playoffs al mejor de tres.",
                LocalDate.of(2026, 11, 15), LocalTime.of(15, 0),
                "Santiago", "Av. Providencia 1234, Providencia",
                List.of("GamerZone", "Liga Andina de Esports"),
                new ServiciosEvento(true, true, true, false),
                List.of("Equipo Andes Esports", "Caster Sofía \"Nova\" Rojas"),
                true),
            new Evento("LAN Party Santiago", "LAN Party",
                "Trae tu equipo y juega toda la noche con la comunidad GamerZone. Mesas, energía y red cableada para 200 jugadores.",
                LocalDate.of(2026, 11, 22), LocalTime.of(18, 0),
                "Santiago", "Centro de Eventos Matucana 456, Estación Central",
                List.of("GamerZone", "Club LAN Chile"),
                new ServiciosEvento(true, true, true, false),
                List.of("Técnicos de HexaGear", "Streamer Diego \"Pixel\" Fuentes"),
                true),
            new Evento("Speedrun Challenge", "Online",
                "¿Eres el más rápido? Demuéstralo en nuestro desafío de speedrun transmitido en vivo.",
                LocalDate.of(2026, 11, 30), LocalTime.of(20, 0),
                "Online", "Transmisión en vivo desde el canal oficial de GamerZone",
                List.of("GamerZone"),
                new ServiciosEvento(false, false, false, false),
                List.of("Comunidad Speedrun Chile"),
                false),
            new Evento("Feria Retro Gaming", "Feria",
                "Consolas clásicas, arcades, coleccionables y zona de juego libre para toda la familia.",
                LocalDate.of(2026, 12, 5), LocalTime.of(11, 0),
                "Valparaíso", "Av. Brasil 2950, Valparaíso",
                List.of("Asociación Retro Porteña", "GamerZone"),
                new ServiciosEvento(true, true, true, true),
                List.of("Coleccionistas de la V Región", "Museo del Videojuego (muestra itinerante)"),
                true),
            new Evento("Torneo FC Clausura", "Torneo",
                "Torneo 1 vs 1 de fútbol virtual con premios para los tres primeros lugares.",
                LocalDate.of(2026, 12, 12), LocalTime.of(16, 0),
                "Concepción", "Calle O'Higgins 780, Concepción",
                List.of("Liga Sur Gaming"),
                new ServiciosEvento(false, true, true, true),
                List.of("Jugador profesional Matías Soto"),
                false),
            new Evento("Encuentro Indie Devs", "Charla",
                "Desarrolladores independientes muestran sus juegos y cuentan cómo los crearon. Incluye ronda de preguntas.",
                LocalDate.of(2026, 12, 19), LocalTime.of(10, 30),
                "Santiago", "Av. Libertador Bernardo O'Higgins 3363, Estación Central",
                List.of("GamerZone", "Colectivo Indie Chile"),
                new ServiciosEvento(true, true, false, false),
                List.of("Estudio Lumen Games", "Estudio Pájaro Azul"),
                false)
        );
    }
}