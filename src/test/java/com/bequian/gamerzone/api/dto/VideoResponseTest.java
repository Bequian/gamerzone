package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VideoResponseTest {

    @Test
    void deArmaElEnlaceSinCookiesDeSeguimiento() {
        VideoResponse video = VideoResponse.de(1L, "dQw4w9WgXcQ", "Tráiler");
        assertThat(video.embedUrl()).isEqualTo("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ");
        assertThat(video.youtubeId()).isEqualTo("dQw4w9WgXcQ");
        assertThat(video.id()).isEqualTo(1L);
    }
}
