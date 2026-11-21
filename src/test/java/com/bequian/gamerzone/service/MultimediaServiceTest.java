package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Pruebas unitarias de la lista blanca de enlaces de YouTube (sin levantar Spring)
class MultimediaServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?feature=share&v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=42",
            "https://m.youtube.com/shorts/dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ"
    })
    void aceptaEnlacesDeYoutube(String url) {
        assertThat(MultimediaService.extraerIdYoutube(url)).isEqualTo("dQw4w9WgXcQ");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://evil.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com.evil.com/watch?v=dQw4w9WgXcQ",
            "javascript:alert(1)",
            "https://www.youtube.com/watch?v=corto",
            "https://youtu.be/dQw4w9WgXcQ\"><script>alert(1)</script>"
    })
    void rechazaEnlacesNoPermitidos(String url) {
        assertThatThrownBy(() -> MultimediaService.extraerIdYoutube(url))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void rechazaEnlaceVacio() {
        assertThatThrownBy(() -> MultimediaService.extraerIdYoutube("  "))
                .isInstanceOf(ValidacionException.class);
    }
}
