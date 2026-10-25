package com.bequian.gamerzone.model;

import java.util.List;

// Perfil guardado de un usuario (inmutable)
public record Perfil(
        String gamertag,
        String avatar,
        List<String> juegos,
        boolean notifEmail,
        boolean notifNuevosEventos,
        boolean notifRecordatorios) {

    public static Perfil porDefecto() {
        return new Perfil("", "control", List.of(), true, true, false);
    }
}