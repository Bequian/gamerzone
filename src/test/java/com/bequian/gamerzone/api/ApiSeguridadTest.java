package com.bequian.gamerzone.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSeguridadTest {

    private static final String EVENTO_JSON = """
            {
              "titulo": "Torneo de prueba",
              "categoria": "Torneo",
              "descripcion": "Evento creado desde un test",
              "fecha": "2030-01-15",
              "hora": "18:00",
              "ubicacion": "Santiago",
              "direccion": "Calle Falsa 123",
              "organizadores": ["GamerZone"],
              "expositores": [],
              "servicios": {"estacionamiento": true, "locomocion": true, "comida": false, "alojamiento": false},
              "destacado": false
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    private String obtenerToken(String usuario, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    // ===== Login =====

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"test-admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ===== API pública =====

    @Test
    void listadoDeEventosEsPublico() throws Exception {
        mockMvc.perform(get("/api/eventos")).andExpect(status().isOk());
    }

    // ===== API privada =====

    @Test
    void detalleSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/eventos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void detalleConTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/eventos/1").header("Authorization", "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void detalleConTokenValidoDevuelve200() throws Exception {
        String token = obtenerToken("user", "test-user");
        mockMvc.perform(get("/api/eventos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ===== Gestión de eventos por rol =====

    @Test
    void usuarioNormalNoPuedeCrearEventos() throws Exception {
        String token = obtenerToken("user", "test-user");
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizadorPuedeCrearYEliminarEventos() throws Exception {
        String token = obtenerToken("organizador", "test-org");

        String respuesta = mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Torneo de prueba"))
                .andReturn().getResponse().getContentAsString();

        Integer id = JsonPath.read(respuesta, "$.id");

        mockMvc.perform(delete("/api/eventos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void crearEventoConDatosInvalidosDevuelve400() throws Exception {
        String token = obtenerToken("admin", "test-admin");
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists());
    }
}