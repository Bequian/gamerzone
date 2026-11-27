package com.bequian.gamerzone.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
class MultimediaApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoService eventoService;

    @Test
    void organizadorSubeDescargaYEliminaFotoYVideo() throws Exception {
        long id = eventoService.crear(DatosPrueba.eventoRequest("Evento multimedia API")).getId();
        String token = DatosPrueba.bearer(mockMvc, "organizador", "test-org");

        long fotoId = DatosPrueba.idDe(mockMvc.perform(multipart("/api/eventos/" + id + "/fotos")
                        .file(DatosPrueba.fotoPng()).header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/eventos/" + id + "/fotos/" + fotoId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));

        long videoId = DatosPrueba.idDe(mockMvc.perform(post("/api/eventos/" + id + "/videos")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://youtu.be/dQw4w9WgXcQ\",\"titulo\":\"Tráiler\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/eventos/" + id + "/multimedia").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotos.length()").value(1))
                .andExpect(jsonPath("$.videos[0].youtubeId").value("dQw4w9WgXcQ"));

        mockMvc.perform(delete("/api/eventos/" + id + "/fotos/" + fotoId).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/eventos/" + id + "/videos/" + videoId).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/eventos/" + id + "/videos/" + videoId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void enlaceQueNoEsDeYoutubeDevuelve400() throws Exception {
        mockMvc.perform(post("/api/eventos/1/videos")
                        .header("Authorization", DatosPrueba.bearer(mockMvc, "organizador", "test-org"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://evil.com/video\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioSinRolNoPuedeSubirFotos() throws Exception {
        mockMvc.perform(multipart("/api/eventos/1/fotos").file(DatosPrueba.fotoPng())
                        .header("Authorization", DatosPrueba.bearer(mockMvc, "user", "test-user")))
                .andExpect(status().isForbidden());
    }
}
