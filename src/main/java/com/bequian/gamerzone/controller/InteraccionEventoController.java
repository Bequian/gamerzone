package com.bequian.gamerzone.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bequian.gamerzone.api.dto.ComentarioRequest;
import com.bequian.gamerzone.service.ComentarioService;
import com.bequian.gamerzone.service.CompartirService;
import com.bequian.gamerzone.service.MultimediaService;
import com.bequian.gamerzone.service.ValidacionException;

import jakarta.validation.Valid;

/**
 * Acciones del sitio web sobre un evento: comentar/valorar, compartir y gestionar fotos y videos.
 * Todas son privadas (requieren sesión) y todos los formularios llevan el token CSRF.
 * Después de cada acción se redirige al detalle (patrón Post/Redirect/Get).
 */
@Controller
@RequestMapping("/eventos/{id}")
public class InteraccionEventoController {

    private final ComentarioService comentarioService;
    private final MultimediaService multimediaService;
    private final CompartirService compartirService;

    public InteraccionEventoController(ComentarioService comentarioService,
                                       MultimediaService multimediaService,
                                       CompartirService compartirService) {
        this.comentarioService = comentarioService;
        this.multimediaService = multimediaService;
        this.compartirService = compartirService;
    }

    // ===== Comentarios y valoraciones (cualquier usuario autenticado) =====

    @PostMapping("/comentarios")
    public String comentar(@PathVariable("id") Long id,
                           @Valid @ModelAttribute ComentarioRequest form,
                           BindingResult result,
                           Authentication auth,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("errorComentario", result.getAllErrors().get(0).getDefaultMessage());
            return volver(id, "comentarios");
        }
        boolean creado = comentarioService.guardar(id, auth.getName(), form).creado();
        redirect.addFlashAttribute("mensajeComentario",
                creado ? "¡Gracias! Tu comentario fue publicado." : "Tu comentario fue actualizado.");
        return volver(id, "comentarios");
    }

    @PostMapping("/comentarios/{comentarioId}/eliminar")
    public String eliminarComentario(@PathVariable("id") Long id,
                                     @PathVariable("comentarioId") Long comentarioId,
                                     Authentication auth,
                                     RedirectAttributes redirect) {
        comentarioService.eliminar(id, comentarioId, auth.getName(), esAdmin(auth));
        redirect.addFlashAttribute("mensajeComentario", "Comentario eliminado.");
        return volver(id, "comentarios");
    }

    // ===== Compartir en GamerZone (cualquier usuario autenticado) =====

    @PostMapping("/compartir")
    public String compartir(@PathVariable("id") Long id, Authentication auth, RedirectAttributes redirect) {
        compartirService.compartir(id, auth.getName());
        redirect.addFlashAttribute("mensajeCompartir", "Evento compartido en tu perfil de GamerZone.");
        return volver(id, "compartir");
    }

    @PostMapping("/compartir/quitar")
    public String dejarDeCompartir(@PathVariable("id") Long id, Authentication auth, RedirectAttributes redirect) {
        compartirService.dejarDeCompartir(id, auth.getName());
        redirect.addFlashAttribute("mensajeCompartir", "El evento ya no aparece en tu perfil.");
        return volver(id, "compartir");
    }

    // ===== Fotos y videos (solo ORGANIZADOR o ADMIN) =====

    // [PRIVADA] Descarga de una foto. Se responde con el tipo validado al subirla (image/png o image/jpeg)
    @GetMapping("/fotos/{fotoId}")
    public ResponseEntity<byte[]> foto(@PathVariable("id") Long id, @PathVariable("fotoId") Long fotoId) {
        MultimediaService.Imagen imagen = multimediaService.obtenerFoto(id, fotoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.tipoContenido()))
                .header("Content-Disposition", "inline")
                .body(imagen.datos());
    }

    @PostMapping("/fotos")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public String subirFoto(@PathVariable("id") Long id,
                            @RequestParam(name = "archivo", required = false) MultipartFile archivo,
                            Authentication auth,
                            RedirectAttributes redirect) {
        try {
            multimediaService.subirFoto(id, auth.getName(), archivo);
            redirect.addFlashAttribute("mensajeMultimedia", "Foto agregada al evento.");
        } catch (ValidacionException e) {
            redirect.addFlashAttribute("errorMultimedia", e.getMessage());
        }
        return volver(id, "multimedia");
    }

    @PostMapping("/fotos/{fotoId}/eliminar")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public String eliminarFoto(@PathVariable("id") Long id, @PathVariable("fotoId") Long fotoId,
                               RedirectAttributes redirect) {
        multimediaService.eliminarFoto(id, fotoId);
        redirect.addFlashAttribute("mensajeMultimedia", "Foto eliminada.");
        return volver(id, "multimedia");
    }

    @PostMapping("/videos")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public String agregarVideo(@PathVariable("id") Long id,
                               @RequestParam(name = "url", required = false) String url,
                               @RequestParam(name = "titulo", required = false) String titulo,
                               Authentication auth,
                               RedirectAttributes redirect) {
        try {
            multimediaService.agregarVideo(id, auth.getName(), url, titulo);
            redirect.addFlashAttribute("mensajeMultimedia", "Video agregado al evento.");
        } catch (ValidacionException e) {
            redirect.addFlashAttribute("errorMultimedia", e.getMessage());
        }
        return volver(id, "multimedia");
    }

    @PostMapping("/videos/{videoId}/eliminar")
    @PreAuthorize("hasAnyRole('ORGANIZADOR', 'ADMIN')")
    public String eliminarVideo(@PathVariable("id") Long id, @PathVariable("videoId") Long videoId,
                                RedirectAttributes redirect) {
        multimediaService.eliminarVideo(id, videoId);
        redirect.addFlashAttribute("mensajeMultimedia", "Video eliminado.");
        return volver(id, "multimedia");
    }

    // ===== Utilidades =====

    private static String volver(Long id, String seccion) {
        return "redirect:/eventos/" + id + "#" + seccion;
    }

    private static boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
