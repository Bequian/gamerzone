package com.bequian.gamerzone;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.api.dto.EventoRequest;
import com.bequian.gamerzone.model.ServiciosEvento;
import com.jayway.jsonpath.JsonPath;

/**
 * Utilidades compartidas por los tests: datos de ejemplo y obtención de tokens.
 * Cada test crea sus propios datos (eventos, usuarios) para no depender del orden de ejecución.
 */
public final class DatosPrueba {

    public static final String PASSWORD_VALIDA = "Clave1234";

    private DatosPrueba() {
    }

    // Correo único para que dos tests nunca choquen con "ya existe una cuenta"
    public static String correoUnico(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.cl";
    }

    public static EventoRequest eventoRequest(String titulo) {
        return new EventoRequest(titulo, "Torneo", "Evento creado desde un test",
                LocalDate.now().plusDays(30), LocalTime.of(18, 0), "Santiago", "Calle Falsa 123",
                List.of("GamerZone"), List.of("Expositor de prueba"),
                new ServiciosEvento(true, true, false, false), false);
    }

    public static String eventoJson(String titulo) {
        return """
                {
                  "titulo": "%s",
                  "categoria": "Torneo",
                  "descripcion": "Evento creado desde un test",
                  "fecha": "2030-03-20",
                  "hora": "18:00",
                  "ubicacion": "Santiago",
                  "direccion": "Calle Falsa 123",
                  "organizadores": ["GamerZone"],
                  "expositores": [],
                  "servicios": {"estacionamiento": true, "locomocion": true, "comida": false, "alojamiento": false},
                  "destacado": false
                }
                """.formatted(titulo);
    }

    // Devuelve la cabecera completa "Bearer <token>"
    public static String bearer(MockMvc mockMvc, String usuario, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(respuesta, "$.token");
    }

    public static byte[] imagen(int ancho, int alto, String formato) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB), formato, salida);
        return salida.toByteArray();
    }

    public static MockMultipartFile fotoPng() throws IOException {
        return new MockMultipartFile("archivo", "foto.png", "image/png", imagen(16, 16, "png"));
    }

    public static long idDe(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
