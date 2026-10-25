package com.bequian.gamerzone.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "gamerzone.user.password=test-user",
    "gamerzone.admin.password=test-admin",
    "gamerzone.organizador.password=test-org"
})
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    // ===== Páginas públicas =====

    @Test
    void inicioEsPublico() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void registroEsPublico() throws Exception {
        mockMvc.perform(get("/registro")).andExpect(status().isOk());
    }

    // ===== Páginas privadas =====

    @Test
    void eventosRequiereLogin() throws Exception {
        mockMvc.perform(get("/eventos"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void perfilRequiereLogin() throws Exception {
        mockMvc.perform(get("/perfil"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void usuarioAutenticadoVeEventos() throws Exception {
        mockMvc.perform(get("/eventos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void eventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/eventos/999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void usuarioNormalNoEntraAAdmin() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    // ===== Login con los 3 usuarios =====

    @Test
    void losTresUsuariosPuedenIniciarSesion() throws Exception {
        mockMvc.perform(formLogin().user("user").password("test-user")).andExpect(authenticated());
        mockMvc.perform(formLogin().user("admin").password("test-admin")).andExpect(authenticated());
        mockMvc.perform(formLogin().user("organizador").password("test-org")).andExpect(authenticated());
    }

    @Test
    void loginConContrasenaIncorrecta() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("incorrecta")).andExpect(unauthenticated());
    }

    // ===== Registro =====

    @Test
    void registroCreaUsuarioQuePuedeIniciarSesion() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                .param("nombre", "Jugador Test")
                .param("correo", "nuevo@test.cl")
                .param("password", "Clave1234")
                .param("confirmarPassword", "Clave1234"))
            .andExpect(redirectedUrl("/login?registrado"));

        mockMvc.perform(formLogin().user("nuevo@test.cl").password("Clave1234"))
            .andExpect(authenticated().withRoles("USER"));
    }

    @Test
    void registroRechazaContrasenaDebil() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                .param("nombre", "Jugador Test")
                .param("correo", "debil@test.cl")
                .param("password", "123")
                .param("confirmarPassword", "123"))
            .andExpect(status().isOk())
            .andExpect(view().name("registro"))
            .andExpect(model().attributeHasFieldErrors("registro", "password"));
    }

    @Test
    void registroSinTokenCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/registro")
                .param("nombre", "Atacante")
                .param("correo", "atacante@test.cl")
                .param("password", "Clave1234")
                .param("confirmarPassword", "Clave1234"))
            .andExpect(status().isForbidden());
    }
}
