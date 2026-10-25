package com.bequian.gamerzone.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Whitelist de constantes que se usan en la aplicación, como avatares y juegos.
public final class Catalogo {

    public static final Map<String, String> AVATARES = crearAvatares();

    public static final List<String> JUEGOS = List.of(
            "Valorant", "League of Legends", "Minecraft", "Fortnite",
            "Counter-Strike 2", "EA Sports FC", "Street Fighter 6", "World of Warcraft");

    private Catalogo() {
        // Clase de constantes: no se instancia
    }

    private static Map<String, String> crearAvatares() {
        Map<String, String> avatares = new LinkedHashMap<>();
        avatares.put("control", "🎮");
        avatares.put("alien", "👾");
        avatares.put("joystick", "🕹️");
        avatares.put("dragon", "🐉");
        avatares.put("robot", "🤖");
        avatares.put("ninja", "🥷");
        avatares.put("mago", "🧙");
        avatares.put("zorro", "🦊");
        return Collections.unmodifiableMap(avatares);
    }
}