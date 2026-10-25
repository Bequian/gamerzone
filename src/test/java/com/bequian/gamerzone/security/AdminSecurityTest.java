package com.bequian.gamerzone.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    "gamerzone.admin.password=test-admin"
})
@AutoConfigureMockMvc
class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonimoEsRedirigidoAlLogin() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void usuarioNormalRecibe403() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void adminPuedeVerElPanel() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/panel"));
    }

    @Test
    void loginConCredencialesCorrectas() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("test-admin"))
            .andExpect(authenticated().withRoles("USER", "ADMIN"));
    }

    @Test
    void loginConContrasenaIncorrecta() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("incorrecta"))
            .andExpect(unauthenticated());
    }

    @Test
    void loginSinTokenCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin").param("password", "test-admin"))
            .andExpect(status().isForbidden());
    }
}