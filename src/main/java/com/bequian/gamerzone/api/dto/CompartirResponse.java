package com.bequian.gamerzone.api.dto;

// Enlaces para compartir un evento en redes sociales y estado de "compartido" dentro de GamerZone
public record CompartirResponse(String url, String whatsapp, String x, String facebook,
                                long totalCompartidos, boolean compartidoPorMi) {
}
