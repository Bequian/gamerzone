package com.bequian.gamerzone.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.DatosPrueba;

@SpringBootTest
@AutoConfigureMockMvc
class EventoApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String organizador() throws Exception {
        return DatosPrueba.bearer(mockMvc, "organizador", "test-org");
    }

    private long crear(String token, String titulo) throws Exception {
        String json = mockMvc.perform(post("/api/eventos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(DatosPrueba.eventoJson(titulo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return DatosPrueba.idDe(json);
    }

    @Test
    void listarEsPublicoYFiltraPorCategoria() throws Exception {
        mockMvc.perform(get("/api/eventos").param("categoria", "Torneo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Torneo"));
    }

    @Test
    void organizadorCreaEditaYEliminaUnEvento() throws Exception {
        String token = organizador();
        long id = crear(token, "Evento para editar");

        mockMvc.perform(put("/api/eventos/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(DatosPrueba.eventoJson("Evento editado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Evento editado"));

        mockMvc.perform(get("/api/eventos/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Evento editado"));

        mockMvc.perform(delete("/api/eventos/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void eventoInexistenteDevuelve404EnDetalleEdicionYEliminacion() throws Exception {
        String token = organizador();
        mockMvc.perform(get("/api/eventos/999999").header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Evento no encontrado"));
        mockMvc.perform(put("/api/eventos/999999").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(DatosPrueba.eventoJson("No existe")))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/eventos/999999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioSinRolNoPuedeEditar() throws Exception {
        String token = DatosPrueba.bearer(mockMvc, "user", "test-user");
        mockMvc.perform(put("/api/eventos/1").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(DatosPrueba.eventoJson("Intento")))
                .andExpect(status().isForbidden());
    }
}
