package com.bequian.gamerzone.funcionalidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.bequian.gamerzone.repository.ComentarioRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.jayway.jsonpath.JsonPath;

/**
 * Pruebas de las funcionalidades de la semana 5: comentarios y valoraciones, fotos y videos,
 * y compartir eventos. Verifican tanto el funcionamiento como las medidas de seguridad.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FuncionalidadesSemana5Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private FotoEventoRepository fotoRepository;

    private static final RequestPostProcessor USUARIO = user("user").roles("USER");
    private static final RequestPostProcessor ORGANIZADOR = user("organizador").roles("USER", "ORGANIZADOR");

    private static byte[] imagenPng(int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", salida);
        return salida.toByteArray();
    }

    private String obtenerToken(String usuario, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    // ===== Comentarios y valoraciones =====

    @Test
    void comentarSinSesionRedirigeAlLogin() throws Exception {
        mockMvc.perform(post("/eventos/1/comentarios").with(csrf())
                        .param("texto", "Hola").param("valoracion", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void comentarSinTokenCsrfEsRechazado() throws Exception {
        mockMvc.perform(post("/eventos/1/comentarios").with(USUARIO)
                        .param("texto", "Hola").param("valoracion", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioComentaYValoraUnEvento() throws Exception {
        mockMvc.perform(post("/eventos/1/comentarios").with(USUARIO).with(csrf())
                        .param("texto", "Excelente torneo, muy bien organizado").param("valoracion", "5"))
                .andExpect(redirectedUrl("/eventos/1#comentarios"));

        mockMvc.perform(get("/eventos/1").with(USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Excelente torneo, muy bien organizado")));
    }

    @Test
    void comentarioConScriptSeMuestraComoTexto() throws Exception {
        mockMvc.perform(post("/eventos/2/comentarios").with(USUARIO).with(csrf())
                        .param("texto", "<script>alert('xss')</script>").param("valoracion", "3"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/eventos/2").with(USUARIO))
                .andExpect(content().string(Matchers.not(Matchers.containsString("<script>alert('xss')</script>"))))
                .andExpect(content().string(Matchers.containsString("&lt;script&gt;")));
    }

    @Test
    void valoracionFueraDeRangoNoSeGuarda() throws Exception {
        mockMvc.perform(post("/eventos/3/comentarios").with(USUARIO).with(csrf())
                        .param("texto", "Valoración inválida").param("valoracion", "9"))
                .andExpect(redirectedUrl("/eventos/3#comentarios"))
                .andExpect(flash().attribute("errorComentario", "La valoración máxima es 5"));

        assertThat(comentarioRepository.findByEventoIdAndUsuarioUsername(3L, "user")).isEmpty();
    }

    @Test
    void usuarioNoPuedeEliminarComentarioAjeno() throws Exception {
        mockMvc.perform(post("/eventos/4/comentarios").with(ORGANIZADOR).with(csrf())
                        .param("texto", "Comentario del organizador").param("valoracion", "4"))
                .andExpect(status().is3xxRedirection());
        Long comentarioId = comentarioRepository.findByEventoIdAndUsuarioUsername(4L, "organizador")
                .orElseThrow().getId();

        mockMvc.perform(post("/eventos/4/comentarios/" + comentarioId + "/eliminar").with(USUARIO).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(comentarioRepository.findById(comentarioId)).isPresent();
    }

    // ===== Fotos y videos =====

    @Test
    void usuarioSinRolOrganizadorNoPuedeSubirFotos() throws Exception {
        MockMultipartFile foto = new MockMultipartFile("archivo", "foto.png", "image/png", imagenPng(20, 20));
        mockMvc.perform(multipart("/eventos/1/fotos").file(foto).with(USUARIO).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizadorSubeFotoYSeSirveComoImagen() throws Exception {
        MockMultipartFile foto = new MockMultipartFile("archivo", "foto.png", "image/png", imagenPng(40, 30));
        mockMvc.perform(multipart("/eventos/5/fotos").file(foto).with(ORGANIZADOR).with(csrf()))
                .andExpect(redirectedUrl("/eventos/5#multimedia"))
                .andExpect(flash().attribute("mensajeMultimedia", "Foto agregada al evento."));

        List<Long> fotos = fotoRepository.findIdsByEvento(5L);
        assertThat(fotos).isNotEmpty();
        mockMvc.perform(get("/eventos/5/fotos/" + fotos.get(0)).with(USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }

    @Test
    void archivoQueNoEsImagenEsRechazadoAunqueDigaPng() throws Exception {
        // Un script disfrazado de imagen: extensión .png y Content-Type image/png falsos
        MockMultipartFile falso = new MockMultipartFile("archivo", "malware.png", "image/png",
                "<?php system($_GET['cmd']); ?>".getBytes());
        mockMvc.perform(multipart("/eventos/1/fotos").file(falso).with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("errorMultimedia", "Solo se permiten imágenes JPG o PNG"));
    }

    @Test
    void imagenMayorA2MBEsRechazada() throws Exception {
        byte[] grande = new byte[3 * 1024 * 1024];
        byte[] firmaPng = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        System.arraycopy(firmaPng, 0, grande, 0, firmaPng.length);
        MockMultipartFile foto = new MockMultipartFile("archivo", "grande.png", "image/png", grande);
        mockMvc.perform(multipart("/eventos/1/fotos").file(foto).with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("errorMultimedia", "La imagen supera el tamaño máximo de 2 MB"));
    }

    @Test
    void videoQueNoEsDeYoutubeEsRechazado() throws Exception {
        mockMvc.perform(post("/eventos/1/videos").with(ORGANIZADOR).with(csrf())
                        .param("url", "https://sitio-malicioso.com/watch?v=dQw4w9WgXcQ"))
                .andExpect(flash().attribute("errorMultimedia",
                        "Solo se permiten enlaces de YouTube (youtube.com o youtu.be)"));
    }

    @Test
    void videoDeYoutubeSeIncrustaEnModoPrivado() throws Exception {
        mockMvc.perform(post("/eventos/6/videos").with(ORGANIZADOR).with(csrf())
                        .param("url", "https://youtu.be/dQw4w9WgXcQ?t=10").param("titulo", "Resumen del evento"))
                .andExpect(flash().attribute("mensajeMultimedia", "Video agregado al evento."));

        mockMvc.perform(get("/eventos/6").with(USUARIO))
                .andExpect(content().string(Matchers.containsString("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ")));
    }

    // ===== Compartir =====

    @Test
    void detalleMuestraEnlacesParaCompartirEnRedes() throws Exception {
        mockMvc.perform(get("/eventos/1").with(USUARIO))
                .andExpect(content().string(Matchers.containsString("https://wa.me/?text=")))
                .andExpect(content().string(Matchers.containsString("https://www.facebook.com/sharer/sharer.php?u=")))
                .andExpect(content().string(Matchers.containsString("rel=\"noopener noreferrer\"")));
    }

    @Test
    void compartirEnGamerZoneApareceEnElPerfil() throws Exception {
        mockMvc.perform(post("/eventos/2/compartir").with(USUARIO).with(csrf()))
                .andExpect(redirectedUrl("/eventos/2#compartir"));

        mockMvc.perform(get("/perfil").with(USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("href=\"/eventos/2\"")));
    }

    // ===== API REST =====

    @Test
    void apiDeComentariosRequiereToken() throws Exception {
        mockMvc.perform(get("/api/eventos/1/comentarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiPermiteComentarConToken() throws Exception {
        String token = obtenerToken("admin", "test-admin");
        mockMvc.perform(post("/api/eventos/3/comentarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Comentario desde la API\",\"valoracion\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor").exists())
                .andExpect(jsonPath("$.valoracion").value(4));

        mockMvc.perform(get("/api/eventos/3/comentarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios[*].texto", Matchers.hasItem("Comentario desde la API")));
    }

    @Test
    void apiNoPermiteSubirFotosAUnUsuarioNormal() throws Exception {
        String token = obtenerToken("user", "test-user");
        MockMultipartFile foto = new MockMultipartFile("archivo", "foto.png", "image/png", imagenPng(10, 10));
        mockMvc.perform(multipart("/api/eventos/1/fotos").file(foto).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
