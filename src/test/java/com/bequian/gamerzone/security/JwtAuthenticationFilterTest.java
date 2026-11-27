package com.bequian.gamerzone.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.DatosPrueba;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void tokenValidoAutentica() throws Exception {
        mockMvc.perform(get("/api/eventos/1").header("Authorization", DatosPrueba.bearer(mockMvc, "user", "test-user")))
                .andExpect(status().isOk());
    }

    @Test
    void cabeceraSinBearerOTokenFalsoQuedaComoAnonimo() throws Exception {
        mockMvc.perform(get("/api/eventos/1").header("Authorization", "Basic dXNlcjp0ZXN0LXVzZXI="))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/eventos/1").header("Authorization", "Bearer no.es.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUnUsuarioQueNoExisteEsRechazado() throws Exception {
        // Firmado correctamente, pero el usuario no está en la BD (por ejemplo, fue eliminado)
        String token = jwtService.generarToken(User.withUsername("fantasma").password("x").roles("ADMIN").build());
        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
