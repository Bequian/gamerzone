package com.bequian.gamerzone.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PerfilFormTest {

    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void convierteDesdeYHaciaPerfil() {
        Perfil original = new Perfil("Gamer_1", "robot", List.of("Minecraft"), false, true, true);

        PerfilForm form = PerfilForm.desde(original);
        assertThat(form.getGamertag()).isEqualTo("Gamer_1");
        assertThat(form.isNotifEmail()).isFalse();
        assertThat(form.isNotifNuevosEventos()).isTrue();
        assertThat(form.isNotifRecordatorios()).isTrue();

        form.setGamertag("  Gamer_2  ");
        assertThat(form.aPerfil().gamertag()).isEqualTo("Gamer_2");
        assertThat(form.aPerfil().juegos()).containsExactly("Minecraft");
    }

    @Test
    void juegosNulosSeTratanComoListaVacia() {
        PerfilForm form = new PerfilForm();
        form.setJuegos(null);
        assertThat(form.getJuegos()).isEmpty();
    }

    @Test
    void validaGamertagYMaximoDeJuegos() {
        PerfilForm form = PerfilForm.desde(Perfil.porDefecto());
        form.setGamertag("<script>");
        form.setJuegos(List.of("a", "b", "c", "d", "e", "f"));
        assertThat(VALIDADOR.validate(form))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("gamertag", "juegos");
    }
}
