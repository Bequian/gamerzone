package com.bequian.gamerzone.model;

// Servicios disponibles en el lugar del evento
public record ServiciosEvento(
        boolean estacionamiento,
        boolean locomocion,
        boolean comida,
        boolean alojamiento) {
}