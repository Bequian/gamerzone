package com.bequian.gamerzone.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

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
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoService eventoService;

    @Test
    void busquedaPublicaFiltraPorCategoria() throws Exception {
        mockMvc.perform(get("/eventos").param("categoria", "Torneo"))
                .andExpect(status().isOk())
                .andExpect(view().name("eventos/lista"))
                .andExpect(model().attribute("categoriaSel", "Torneo"))
                .andExpect(model().attribute("eventos",
                        Matchers.everyItem(Matchers.hasProperty("categoria", Matchers.is("Torneo")))));
    }

    @Test
    void rangoDeFechasInvertidoMuestraError() throws Exception {
        mockMvc.perform(get("/eventos").param("desde", "2030-02-01").param("hasta", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorFechas"))
                .andExpect(model().attribute("eventos", List.of()));
    }

    @Test
    void detalleRequiereSesionYMuestraElEvento() throws Exception {
        long id = eventoService.crear(DatosPrueba.eventoRequest("Evento detalle web")).getId();

        mockMvc.perform(get("/eventos/" + id)).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/eventos/" + id).with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("eventos/detalle"))
                .andExpect(model().attribute("estrellasPromedio", 0))
                .andExpect(model().attributeExists("evento", "comentarios", "fotos", "videos", "compartir"));
    }

    @Test
    void eventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/eventos/999999").with(user("user").roles("USER")))
                .andExpect(status().isNotFound());
    }
}
