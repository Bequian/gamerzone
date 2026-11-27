package com.bequian.gamerzone.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.UsuarioRepository;

/**
 * [Privada - ADMIN] Gestión de usuarios desde el panel web:
 * acceso solo para ADMIN, CSRF obligatorio, validaciones y reglas que protegen a los administradores.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioAdminControllerTest {

    private static final String RUTA = "/admin/usuarios";
    private static final RequestPostProcessor ADMIN = user("admin").roles("USER", "ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario crearDesdeLaWeb(String correo, String... roles) throws Exception {
        var peticion = post(RUTA + "/nuevo").with(ADMIN).with(csrf())
                .param("nombre", "Usuario web").param("correo", correo).param("password", DatosPrueba.PASSWORD_VALIDA);
        for (String rol : roles) {
            peticion.param("roles", rol);
        }
        mockMvc.perform(peticion)
                .andExpect(redirectedUrl(RUTA))
                .andExpect(flash().attribute("mensaje", "Usuario " + correo + " creado"));
        return usuarioRepository.findByUsername(correo).orElseThrow();
    }

    // ===== Control de acceso =====

    @Test
    void soloAdminPuedeEntrar() throws Exception {
        mockMvc.perform(get(RUTA)).andExpect(status().is3xxRedirection());   // anónimo: al login
        mockMvc.perform(get(RUTA).with(user("user").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(get(RUTA).with(user("organizador").roles("USER", "ORGANIZADOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(RUTA).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/usuarios"))
                .andExpect(model().attribute("usuarios", Matchers.not(Matchers.empty())))
                .andExpect(content().string(Matchers.containsString("<td>admin</td>")))
                .andExpect(model().attributeExists("totalAdmins"));
    }

    @Test
    void usuarioSinRolAdminNoPuedeCrearUsuarios() throws Exception {
        mockMvc.perform(post(RUTA + "/nuevo").with(user("user").roles("USER")).with(csrf())
                        .param("nombre", "Intruso").param("correo", DatosPrueba.correoUnico("intruso"))
                        .param("password", DatosPrueba.PASSWORD_VALIDA).param("roles", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearSinTokenCsrfEsRechazado() throws Exception {
        mockMvc.perform(post(RUTA + "/nuevo").with(ADMIN)
                        .param("nombre", "Sin CSRF").param("correo", DatosPrueba.correoUnico("csrf"))
                        .param("password", DatosPrueba.PASSWORD_VALIDA))
                .andExpect(status().isForbidden());
    }

    // ===== Crear, editar y eliminar =====

    @Test
    void muestraElFormularioDeNuevoUsuario() throws Exception {
        mockMvc.perform(get(RUTA + "/nuevo").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/usuario-form"))
                .andExpect(model().attribute("rolesDisponibles", Matchers.contains("USER", "ORGANIZADOR", "ADMIN")));
    }

    @Test
    void adminCreaEditaYEliminaUnUsuario() throws Exception {
        String correo = DatosPrueba.correoUnico("web");
        Usuario creado = crearDesdeLaWeb(correo, "ORGANIZADOR");
        assertThat(creado.getRoles()).containsExactlyInAnyOrder("USER", "ORGANIZADOR");

        mockMvc.perform(get(RUTA + "/" + creado.getId() + "/editar").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(model().attribute("usuarioId", creado.getId()));

        mockMvc.perform(post(RUTA + "/" + creado.getId() + "/editar").with(ADMIN).with(csrf())
                        .param("nombre", "Nombre editado").param("roles", "ADMIN").param("password", ""))
                .andExpect(redirectedUrl(RUTA))
                .andExpect(flash().attribute("mensaje", "Usuario " + correo + " actualizado"));
        Usuario editado = usuarioRepository.findById(creado.getId()).orElseThrow();
        assertThat(editado.getNombre()).isEqualTo("Nombre editado");
        assertThat(editado.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(editado.getPassword()).isEqualTo(creado.getPassword());   // contraseña vacía: no cambia

        mockMvc.perform(post(RUTA + "/" + creado.getId() + "/eliminar").with(ADMIN).with(csrf()))
                .andExpect(redirectedUrl(RUTA))
                .andExpect(flash().attribute("mensaje", "Usuario eliminado"));
        assertThat(usuarioRepository.findById(creado.getId())).isEmpty();
    }

    @Test
    void datosInvalidosVuelvenAlFormularioSinDevolverLaContrasena() throws Exception {
        mockMvc.perform(post(RUTA + "/nuevo").with(ADMIN).with(csrf())
                        .param("nombre", "Débil").param("correo", DatosPrueba.correoUnico("debil"))
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/usuario-form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("usuario", Matchers.hasProperty("password", Matchers.nullValue())));

        mockMvc.perform(post(RUTA + "/nuevo").with(ADMIN).with(csrf())
                        .param("nombre", "Rol falso").param("correo", DatosPrueba.correoUnico("rol"))
                        .param("password", DatosPrueba.PASSWORD_VALIDA).param("roles", "ROOT"))
                .andExpect(model().attribute("error", "Rol no permitido"));
    }

    @Test
    void adminNoPuedeQuitarseSuRolNiEliminarse() throws Exception {
        long idAdmin = usuarioRepository.findByUsername("admin").orElseThrow().getId();

        mockMvc.perform(post(RUTA + "/" + idAdmin + "/editar").with(ADMIN).with(csrf())
                        .param("nombre", "Administrador").param("roles", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/usuario-form"))
                .andExpect(model().attribute("error", "No puedes quitarte tu propio rol de administrador"));

        mockMvc.perform(post(RUTA + "/" + idAdmin + "/eliminar").with(ADMIN).with(csrf()))
                .andExpect(redirectedUrl(RUTA))
                .andExpect(flash().attribute("error", "No puedes eliminar tu propia cuenta"));

        assertThat(usuarioRepository.findByUsername("admin").orElseThrow().getRoles()).contains("ADMIN");
    }

    @Test
    void usuarioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get(RUTA + "/999999/editar").with(ADMIN)).andExpect(status().isNotFound());
    }
}
