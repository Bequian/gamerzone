package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CompartirResponseTest {

    @Test
    void guardaEnlacesYEstado() {
        CompartirResponse r = new CompartirResponse("https://gz/eventos/1", "https://wa.me/?text=x",
                "https://x.com/intent/post?text=x", "https://www.facebook.com/sharer/sharer.php?u=x", 3, true);
        assertThat(r.url()).isEqualTo("https://gz/eventos/1");
        assertThat(r.whatsapp()).startsWith("https://wa.me/");
        assertThat(r.x()).startsWith("https://x.com/");
        assertThat(r.facebook()).contains("facebook.com");
        assertThat(r.totalCompartidos()).isEqualTo(3);
        assertThat(r.compartidoPorMi()).isTrue();
    }
}
