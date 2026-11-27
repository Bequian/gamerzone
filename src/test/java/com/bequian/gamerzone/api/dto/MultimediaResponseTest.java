package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class MultimediaResponseTest {

    @Test
    void agrupaFotosYVideos() {
        MultimediaResponse r = new MultimediaResponse(List.of(new FotoResponse(1L, "/f/1")),
                List.of(VideoResponse.de(2L, "dQw4w9WgXcQ", "Tráiler")));
        assertThat(r.fotos()).hasSize(1);
        assertThat(r.videos()).extracting(VideoResponse::titulo).containsExactly("Tráiler");
    }
}
