package com.bequian.gamerzone.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.service.ComentarioService;
import com.bequian.gamerzone.service.EventoService;
import com.bequian.gamerzone.service.MultimediaService;

@SpringBootTest
@AutoConfigureMockMvc
class InteraccionEventoControllerTest {

    private static final RequestPostProcessor USUARIO = user("user").roles("USER");
    private static final RequestPostProcessor ORGANIZADOR = user("organizador").roles("USER", "ORGANIZADOR");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private MultimediaService multimediaService;

    @Autowired
    private ComentarioService comentarioService;

    private long nuevoEvento(String titulo) {
        return eventoService.crear(DatosPrueba.eventoRequest(titulo)).getId();
    }

    @Test
    void comentarPorPrimeraVezYLuegoActualizar() throws Exception {
        long id = nuevoEvento("Evento comentarios web");
        String ruta = "/eventos/" + id + "/comentarios";

        mockMvc.perform(post(ruta).with(USUARIO).with(csrf()).param("texto", "Muy bueno").param("valoracion", "4"))
                .andExpect(redirectedUrl("/eventos/" + id + "#comentarios"))
                .andExpect(flash().attribute("mensajeComentario", "¡Gracias! Tu comentario fue publicado."));
        mockMvc.perform(post(ruta).with(USUARIO).with(csrf()).param("texto", "Excelente").param("valoracion", "5"))
                .andExpect(flash().attribute("mensajeComentario", "Tu comentario fue actualizado."));
    }

    @Test
    void comentarioInvalidoVuelveConError() throws Exception {
        long id = nuevoEvento("Evento comentario inválido");
        mockMvc.perform(post("/eventos/" + id + "/comentarios").with(USUARIO).with(csrf())
                        .param("texto", "").param("valoracion", "4"))
                .andExpect(flash().attributeExists("errorComentario"));
    }

    @Test
    void comentarSinTokenCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/eventos/1/comentarios").with(USUARIO).param("texto", "Hola").param("valoracion", "4"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEliminaComentarioDeOtroUsuario() throws Exception {
        long id = nuevoEvento("Evento eliminar comentario web");
        mockMvc.perform(post("/eventos/" + id + "/comentarios").with(USUARIO).with(csrf())
                .param("texto", "Hola").param("valoracion", "3"));
        long comentarioId = eventoComentarioId(id);

        mockMvc.perform(post("/eventos/" + id + "/comentarios/" + comentarioId + "/eliminar")
                        .with(user("admin").roles("USER", "ADMIN")).with(csrf()))
                .andExpect(flash().attribute("mensajeComentario", "Comentario eliminado."));
    }

    private long eventoComentarioId(long eventoId) {
        return comentarioService.listar(eventoId, "user").comentarios().get(0).id();
    }

    @Test
    void compartirYQuitarDelPerfil() throws Exception {
        long id = nuevoEvento("Evento compartir web");
        mockMvc.perform(post("/eventos/" + id + "/compartir").with(USUARIO).with(csrf()))
                .andExpect(redirectedUrl("/eventos/" + id + "#compartir"))
                .andExpect(flash().attributeExists("mensajeCompartir"));
        mockMvc.perform(post("/eventos/" + id + "/compartir/quitar").with(USUARIO).with(csrf()))
                .andExpect(flash().attribute("mensajeCompartir", "El evento ya no aparece en tu perfil."));
    }

    @Test
    void organizadorGestionaFotosYVideosDesdeLaWeb() throws Exception {
        long id = nuevoEvento("Evento multimedia web");

        mockMvc.perform(multipart("/eventos/" + id + "/fotos").file(DatosPrueba.fotoPng()).with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("mensajeMultimedia", "Foto agregada al evento."));
        long fotoId = multimediaService.idsFotos(id).get(0);

        mockMvc.perform(get("/eventos/" + id + "/fotos/" + fotoId).with(USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));

        mockMvc.perform(post("/eventos/" + id + "/videos").with(ORGANIZADOR).with(csrf())
                        .param("url", "https://youtu.be/dQw4w9WgXcQ").param("titulo", "Tráiler"))
                .andExpect(flash().attribute("mensajeMultimedia", "Video agregado al evento."));
        long videoId = multimediaService.videos(id).get(0).id();

        mockMvc.perform(post("/eventos/" + id + "/fotos/" + fotoId + "/eliminar").with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("mensajeMultimedia", "Foto eliminada."));
        mockMvc.perform(post("/eventos/" + id + "/videos/" + videoId + "/eliminar").with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("mensajeMultimedia", "Video eliminado."));
    }

    @Test
    void erroresDeMultimediaSeMuestranComoMensaje() throws Exception {
        long id = nuevoEvento("Evento multimedia errores web");
        mockMvc.perform(post("/eventos/" + id + "/videos").with(ORGANIZADOR).with(csrf()).param("url", "https://evil.com"))
                .andExpect(flash().attributeExists("errorMultimedia"));
        mockMvc.perform(multipart("/eventos/" + id + "/fotos").with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("errorMultimedia", "Selecciona una imagen"));
    }

    @Test
    void usuarioSinRolNoPuedeAgregarMultimedia() throws Exception {
        mockMvc.perform(post("/eventos/1/videos").with(USUARIO).with(csrf()).param("url", "https://youtu.be/dQw4w9WgXcQ"))
                .andExpect(status().isForbidden());
    }
}
