package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.ServiciosEvento;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class EventoRequestTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceptaEventoValido() {
        assertThat(VALIDADOR.validate(DatosPrueba.eventoRequest("Evento válido"))).isEmpty();
    }

    @Test
    void rechazaFechaPasadaSinOrganizadoresYCamposVacios() {
        EventoRequest invalido = new EventoRequest("", "", "", LocalDate.now().minusDays(1), LocalTime.NOON,
                "", "", List.of(), List.of(), new ServiciosEvento(false, false, false, false), null);
        assertThat(VALIDADOR.validate(invalido))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("titulo", "categoria", "descripcion", "fecha", "ubicacion", "direccion",
                        "organizadores", "destacado");
    }
}
