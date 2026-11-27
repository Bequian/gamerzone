package com.bequian.gamerzone.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contrasenasSeCifranConBCrypt() {
        String hash = passwordEncoder.encode("Clave1234");
        assertThat(hash).startsWith("$2").isNotEqualTo("Clave1234");
        assertThat(passwordEncoder.matches("Clave1234", hash)).isTrue();
        assertThat(passwordEncoder.matches("otra", hash)).isFalse();
    }

    @Test
    void sitioWebEnviaCabecerasDeSeguridad() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", Matchers.containsString("default-src 'self'")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    void apiPrivadaSinTokenDevuelve401YRecursosEstaticosSonPublicos() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/css/styles.css")).andExpect(status().isOk());
    }
}
