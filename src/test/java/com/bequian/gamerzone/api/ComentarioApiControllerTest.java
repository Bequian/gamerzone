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
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@AutoConfigureMockMvc
class ComentarioApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoService eventoService;

    @Test
    void comentarActualizarListarYEliminar() throws Exception {
        long id = eventoService.crear(DatosPrueba.eventoRequest("Evento comentarios API")).getId();
        String token = DatosPrueba.bearer(mockMvc, "user", "test-user");
        String ruta = "/api/eventos/" + id + "/comentarios";

        String json = mockMvc.perform(post(ruta).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Muy bueno\",\"valoracion\":4}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long comentarioId = DatosPrueba.idDe(json);

        // Segundo comentario del mismo usuario: actualiza (200)
        mockMvc.perform(post(ruta).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Excelente\",\"valoracion\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("Excelente"));

        mockMvc.perform(get(ruta).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.promedio").value(5.0))
                .andExpect(jsonPath("$.comentarios[0].propio").value(true));

        // Otro usuario (organizador, sin ADMIN) no puede eliminarlo
        mockMvc.perform(delete(ruta + "/" + comentarioId)
                        .header("Authorization", DatosPrueba.bearer(mockMvc, "organizador", "test-org")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete(ruta + "/" + comentarioId).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void valoracionFueraDeRangoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/eventos/1/comentarios")
                        .header("Authorization", DatosPrueba.bearer(mockMvc, "user", "test-user"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\",\"valoracion\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.valoracion").exists());
    }

    @Test
    void comentarioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/eventos/1/comentarios/999999")
                        .header("Authorization", DatosPrueba.bearer(mockMvc, "admin", "test-admin")))
                .andExpect(status().isNotFound());
    }
}
