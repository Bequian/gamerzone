package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.model.Perfil;
import com.bequian.gamerzone.model.RegistroForm;
import com.bequian.gamerzone.repository.UsuarioRepository;

@SpringBootTest
class UsuarioServiceTest {

    @Autowired
    private UsuarioService servicio;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static RegistroForm form(String correo) {
        RegistroForm form = new RegistroForm();
        form.setNombre("  Nueva Persona ");
        form.setCorreo(correo);
        form.setPassword(DatosPrueba.PASSWORD_VALIDA);
        form.setConfirmarPassword(DatosPrueba.PASSWORD_VALIDA);
        return form;
    }

    @Test
    void registraConRolUserYRechazaCorreoRepetido() {
        String correo = DatosPrueba.correoUnico("servicio");
        assertThat(servicio.registrar(form("  " + correo.toUpperCase() + " "))).isTrue();
        assertThat(servicio.registrar(form(correo))).isFalse();

        var guardado = usuarioRepository.findByUsername(correo).orElseThrow();
        assertThat(guardado.getRoles()).containsExactly("USER");   // el rol lo decide el servidor
        assertThat(guardado.getNombre()).isEqualTo("Nueva Persona");
        assertThat(servicio.nombreDe(correo)).isEqualTo("Nueva Persona");
    }

    @Test
    void usuarioInexistenteUsaValoresPorDefecto() {
        assertThat(servicio.nombreDe("fantasma")).isEqualTo("fantasma");
        assertThat(servicio.nombreDe(null)).isNull();
        assertThat(servicio.obtenerPerfil("fantasma")).isEqualTo(Perfil.porDefecto());
        servicio.guardarPerfil("fantasma", Perfil.porDefecto());   // no falla ni crea usuarios
        assertThat(usuarioRepository.existsByUsername("fantasma")).isFalse();
    }

    @Test
    void guardaElPerfil() {
        String correo = DatosPrueba.correoUnico("perfil");
        servicio.registrar(form(correo));
        Perfil perfil = new Perfil("Jugador_9", "dragon", List.of("Valorant"), false, false, true);

        servicio.guardarPerfil(correo, perfil);

        assertThat(servicio.obtenerPerfil(correo)).isEqualTo(perfil);
    }
}
