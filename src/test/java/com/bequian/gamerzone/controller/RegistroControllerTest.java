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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.DatosPrueba;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void muestraElFormulario() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attributeExists("registro"));
    }

    @Test
    void registraYLuegoRechazaElMismoCorreo() throws Exception {
        String correo = DatosPrueba.correoUnico("registro");
        mockMvc.perform(post("/registro").with(csrf()).param("nombre", "Nuevo").param("correo", correo)
                        .param("password", "Clave1234").param("confirmarPassword", "Clave1234"))
                .andExpect(redirectedUrl("/login?registrado"));

        mockMvc.perform(post("/registro").with(csrf()).param("nombre", "Otro").param("correo", correo)
                        .param("password", "Clave1234").param("confirmarPassword", "Clave1234"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("registro", "correo", "duplicado"));
    }

    @Test
    void contrasenasDistintasSeRechazan() throws Exception {
        mockMvc.perform(post("/registro").with(csrf()).param("nombre", "Nuevo")
                        .param("correo", DatosPrueba.correoUnico("distintas"))
                        .param("password", "Clave1234").param("confirmarPassword", "Clave9999"))
                .andExpect(view().name("registro"))
                .andExpect(model().attributeHasFieldErrorCode("registro", "confirmarPassword", "noCoincide"));
    }
}
