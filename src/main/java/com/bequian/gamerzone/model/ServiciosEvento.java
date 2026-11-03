package com.bequian.gamerzone.model;

import jakarta.persistence.Embeddable;

// Servicios disponibles en el lugar del evento.
// @Embeddable: sus campos se guardan como columnas dentro de la tabla "evento".
@Embeddable
public record ServiciosEvento(
        boolean estacionamiento,
        boolean locomocion,
        boolean comida,
        boolean alojamiento) {
}