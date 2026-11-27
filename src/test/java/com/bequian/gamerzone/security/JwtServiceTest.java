package com.bequian.gamerzone.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.JwtException;

// Prueba unitaria: se crea el servicio con una clave de prueba, sin levantar Spring
class JwtServiceTest {

    private static final String CLAVE = "dGVzdC1zZWNyZXQtcGFyYS1qd3QtZ2FtZXJ6b25lLTMyYg==";
    private static final String OTRA_CLAVE = "b3RyYS1jbGF2ZS1kaXN0aW50YS1wYXJhLWp3dC0zMmJ5dGVz";

    private final JwtService servicio = new JwtService(CLAVE, 60);
    private final UserDetails usuario = User.withUsername("admin").password("x").roles("USER", "ADMIN").build();

    @Test
    void generaUnTokenDelQueSeRecuperaElUsuario() {
        String token = servicio.generarToken(usuario);
        assertThat(token.split("\\.")).hasSize(3);   // cabecera.carga.firma
        assertThat(servicio.extraerUsername(token)).isEqualTo("admin");
        assertThat(servicio.getExpiracionSegundos()).isEqualTo(3600);
    }

    @Test
    void rechazaTokensAlteradosOFirmadosConOtraClave() {
        String token = servicio.generarToken(usuario);
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        assertThatThrownBy(() -> servicio.extraerUsername(alterado)).isInstanceOf(JwtException.class);

        String ajeno = new JwtService(OTRA_CLAVE, 60).generarToken(usuario);
        assertThatThrownBy(() -> servicio.extraerUsername(ajeno)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaTokensExpirados() {
        String expirado = new JwtService(CLAVE, -1).generarToken(usuario);
        assertThatThrownBy(() -> servicio.extraerUsername(expirado)).isInstanceOf(JwtException.class);
    }
}
