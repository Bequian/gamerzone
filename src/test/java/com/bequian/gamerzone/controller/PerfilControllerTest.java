package com.bequian.gamerzone.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.bequian.gamerzone.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void perfilRequiereSesion() throws Exception {
        mockMvc.perform(get("/perfil")).andExpect(status().is3xxRedirection());
    }

    @Test
    void muestraElPerfilDelUsuario() throws Exception {
        mockMvc.perform(get("/perfil").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil"))
                .andExpect(model().attributeExists("perfilForm", "avatares", "juegosDisponibles", "compartidos"));
    }

    @Test
    void avatarOJuegoFueraDeLaListaSeRechazan() throws Exception {
        mockMvc.perform(post("/perfil").with(user("user").roles("USER")).with(csrf())
                        .param("gamertag", "Jugador_1").param("avatar", "hacker").param("juegos", "Tetris"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil"))
                .andExpect(model().attributeHasFieldErrors("perfilForm", "avatar", "juegos"));
    }

    @Test
    void guardaUnPerfilValido() throws Exception {
        // Se usa el organizador para no alterar el perfil que revisan otros tests
        mockMvc.perform(post("/perfil").with(user("organizador").roles("USER", "ORGANIZADOR")).with(csrf())
                        .param("gamertag", "OrgGamer").param("avatar", "robot").param("juegos", "Minecraft")
                        .param("notifEmail", "true"))
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("mensaje", "Perfil actualizado correctamente."));

        assertThat(usuarioService.obtenerPerfil("organizador").gamertag())
                .isEqualTo("OrgGamer");
    }
}
