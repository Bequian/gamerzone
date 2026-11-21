package com.bequian.gamerzone.api.dto;

// Video de un evento. embedUrl usa youtube-nocookie.com (modo de privacidad mejorada de YouTube)
public record VideoResponse(Long id, String youtubeId, String titulo, String embedUrl) {

    public static VideoResponse de(Long id, String youtubeId, String titulo) {
        return new VideoResponse(id, youtubeId, titulo, "https://www.youtube-nocookie.com/embed/" + youtubeId);
    }
}
