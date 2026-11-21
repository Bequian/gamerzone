package com.bequian.gamerzone.api.dto;

import java.util.List;

// Fotos y videos de un evento
public record MultimediaResponse(List<FotoResponse> fotos, List<VideoResponse> videos) {
}
