package com.bequian.gamerzone.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.service.EventoService;

@SpringBootTest
@AutoConfigureMockMvc
class CompartirApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoService eventoService;

    @Test
    void compartirYDejarDeCompartir() throws Exception {
        long id = eventoService.crear(DatosPrueba.eventoRequest("Evento compartir API")).getId();
        String token = DatosPrueba.bearer(mockMvc, "user", "test-user");
        String ruta = "/api/eventos/" + id + "/compartir";

        mockMvc.perform(get(ruta).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsapp").value(Matchers.startsWith("https://wa.me/")))
                .andExpect(jsonPath("$.compartidoPorMi").value(false));

        mockMvc.perform(post(ruta).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCompartidos").value(1))
                .andExpect(jsonPath("$.compartidoPorMi").value(true));

        mockMvc.perform(delete(ruta).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartidoPorMi").value(false));
    }

    @Test
    void eventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/eventos/999999/compartir")
                        .header("Authorization", DatosPrueba.bearer(mockMvc, "user", "test-user")))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/eventos/1/compartir")).andExpect(status().isUnauthorized());
    }
}
