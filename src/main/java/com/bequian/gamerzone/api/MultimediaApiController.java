package com.bequian.gamerzone.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bequian.gamerzone.api.dto.FotoResponse;
import com.bequian.gamerzone.api.dto.MultimediaResponse;
import com.bequian.gamerzone.api.dto.VideoRequest;
import com.bequian.gamerzone.api.dto.VideoResponse;
import com.bequian.gamerzone.service.MultimediaService;

import jakarta.validation.Valid;

// [PRIVADA] Fotos y videos de un evento. Ver: cualquier usuario con token. Agregar/eliminar: ORGANIZADOR o ADMIN
@RestController
@RequestMapping("/api/eventos/{id}")
public class MultimediaApiController {

    private final MultimediaService multimediaService;

    public MultimediaApiController(MultimediaService multimediaService) {
        this.multimediaService = multimediaService;
    }

    @GetMapping("/multimedia")
    public MultimediaResponse listar(@PathVariable("id") Long id) {
        List<FotoResponse> fotos = multimediaService.idsFotos(id).stream()
                .map(fotoId -> new FotoResponse(fotoId, urlFoto(id, fotoId)))
                .toList();
        return new MultimediaResponse(fotos, multimediaService.videos(id));
    }

    @GetMapping("/fotos/{fotoId}")
    public ResponseEntity<byte[]> foto(@PathVariable("id") Long id, @PathVariable("fotoId") Long fotoId) {
        MultimediaService.Imagen imagen = multimediaService.obtenerFoto(id, fotoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.tipoContenido()))
                .body(imagen.datos());
    }

    // Subida en formato multipart/form-data con el campo "archivo"
    @PostMapping(value = "/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public ResponseEntity<FotoResponse> subirFoto(@PathVariable("id") Long id,
                                                  @RequestParam("archivo") MultipartFile archivo,
                                                  Authentication auth) {
        Long fotoId = multimediaService.subirFoto(id, auth.getName(), archivo);
        FotoResponse respuesta = new FotoResponse(fotoId, urlFoto(id, fotoId));
        return ResponseEntity.created(URI.create(respuesta.url())).body(respuesta);
    }

    @DeleteMapping("/fotos/{fotoId}")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminarFoto(@PathVariable("id") Long id, @PathVariable("fotoId") Long fotoId) {
        multimediaService.eliminarFoto(id, fotoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/videos")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public ResponseEntity<VideoResponse> agregarVideo(@PathVariable("id") Long id,
                                                      @Valid @RequestBody VideoRequest request,
                                                      Authentication auth) {
        Long videoId = multimediaService.agregarVideo(id, auth.getName(), request.url(), request.titulo());
        VideoResponse video = multimediaService.videos(id).stream()
                .filter(v -> v.id().equals(videoId)).findFirst().orElseThrow();
        return ResponseEntity.created(URI.create("/api/eventos/" + id + "/multimedia")).body(video);
    }

    @DeleteMapping("/videos/{videoId}")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminarVideo(@PathVariable("id") Long id, @PathVariable("videoId") Long videoId) {
        multimediaService.eliminarVideo(id, videoId);
        return ResponseEntity.noContent().build();
    }

    private static String urlFoto(Long eventoId, Long fotoId) {
        return "/api/eventos/" + eventoId + "/fotos/" + fotoId;
    }
}
