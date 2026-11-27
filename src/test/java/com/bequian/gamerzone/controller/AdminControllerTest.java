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

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminVePanelConTotalesDeEventosYUsuarios() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("USER", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/panel"))
                .andExpect(model().attributeExists("eventos", "totalEventos", "totalUsuarios"));
    }

    @Test
    void usuarioSinRolAdminRecibe403YAnonimoVaAlLogin() throws Exception {
        mockMvc.perform(get("/admin").with(user("user").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
    }
}
