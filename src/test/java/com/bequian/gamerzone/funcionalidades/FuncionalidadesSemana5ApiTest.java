package com.bequian.gamerzone.funcionalidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import com.bequian.gamerzone.repository.VideoEventoRepository;
import com.jayway.jsonpath.JsonPath;

/**
 * Pruebas complementarias de la semana 5: APIs REST de comentarios, multimedia y compartir,
 * acciones de eliminación, límites por evento y manejo de errores.
 * Cada prueba que modifica datos crea su propio evento para no depender de las demás.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FuncionalidadesSemana5ApiTest {

    private static final String EVENTO_JSON = """
            {
              "titulo": "Evento de prueba semana 5",
              "categoria": "Torneo",
              "descripcion": "Evento creado desde un test",
              "fecha": "2030-03-20",
              "hora": "18:00",
              "ubicacion": "Santiago",
              "direccion": "Calle Falsa 123",
              "organizadores": ["GamerZone"],
              "expositores": [],
              "servicios": {"estacionamiento": true, "locomocion": true, "comida": false, "alojamiento": false},
              "destacado": false
            }
            """;

    private static final RequestPostProcessor USUARIO = user("user").roles("USER");
    private static final RequestPostProcessor ORGANIZADOR = user("organizador").roles("USER", "ORGANIZADOR");
    private static final RequestPostProcessor ADMIN = user("admin").roles("USER", "ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private FotoEventoRepository fotoRepository;

    @Autowired
    private VideoEventoRepository videoRepository;

    // ===== Utilidades =====

    private static byte[] imagenPng() throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB), "png", salida);
        return salida.toByteArray();
    }

    private static MockMultipartFile foto() throws IOException {
        return new MockMultipartFile("archivo", "foto.png", "image/png", imagenPng());
    }

    private String token(String usuario, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(respuesta, "$.token");
    }

    private String tokenOrganizador() throws Exception {
        return token("organizador", "test-org");
    }

    private long crearEvento() throws Exception {
        String respuesta = mockMvc.perform(post("/api/eventos")
                        .header("Authorization", tokenOrganizador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    // ===== API de multimedia =====

    @Test
    void apiOrganizadorGestionaFotosYVideos() throws Exception {
        long id = crearEvento();
        String token = tokenOrganizador();

        String foto = mockMvc.perform(multipart("/api/eventos/" + id + "/fotos").file(foto())
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(Matchers.startsWith("/api/eventos/" + id + "/fotos/")))
                .andReturn().getResponse().getContentAsString();
        long fotoId = ((Number) JsonPath.read(foto, "$.id")).longValue();

        String video = mockMvc.perform(post("/api/eventos/" + id + "/videos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\",\"titulo\":\"Tráiler\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.youtubeId").value("dQw4w9WgXcQ"))
                .andExpect(jsonPath("$.embedUrl").value("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ"))
                .andReturn().getResponse().getContentAsString();
        long videoId = ((Number) JsonPath.read(video, "$.id")).longValue();

        // Cualquier usuario con token puede ver la multimedia
        String tokenUsuario = token("user", "test-user");
        mockMvc.perform(get("/api/eventos/" + id + "/multimedia").header("Authorization", tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotos.length()").value(1))
                .andExpect(jsonPath("$.videos[0].titulo").value("Tráiler"));
        mockMvc.perform(get("/api/eventos/" + id + "/fotos/" + fotoId).header("Authorization", tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        mockMvc.perform(delete("/api/eventos/" + id + "/fotos/" + fotoId).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/eventos/" + id + "/videos/" + videoId).header("Authorization", token))
                .andExpect(status().isNoContent());
        assertThat(fotoRepository.countByEventoId(id)).isZero();
        assertThat(videoRepository.countByEventoId(id)).isZero();
    }

    @Test
    void apiRechazaVideoQueNoEsDeYoutube() throws Exception {
        mockMvc.perform(post("/api/eventos/1/videos")
                        .header("Authorization", tokenOrganizador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://evil.com/video\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo se permiten enlaces de YouTube (youtube.com o youtu.be)"));
    }

    @Test
    void apiRechazaSubidaSinArchivoOConArchivoFalso() throws Exception {
        String token = tokenOrganizador();
        mockMvc.perform(multipart("/api/eventos/1/fotos").header("Authorization", token))
                .andExpect(status().isBadRequest());

        MockMultipartFile falso = new MockMultipartFile("archivo", "x.jpg", "image/jpeg", "no soy una imagen".getBytes());
        mockMvc.perform(multipart("/api/eventos/1/fotos").file(falso).header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo se permiten imágenes JPG o PNG"));
    }

    @Test
    void apiFotoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/eventos/1/fotos/999999").header("Authorization", token("user", "test-user")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Foto no encontrada"));
    }

    @Test
    void apiUsuarioNormalNoPuedeEliminarMultimedia() throws Exception {
        mockMvc.perform(delete("/api/eventos/1/videos/1").header("Authorization", token("user", "test-user")))
                .andExpect(status().isForbidden());
    }

    // ===== API de comentarios =====

    @Test
    void apiComentarioSeActualizaYSeElimina() throws Exception {
        long id = crearEvento();
        String token = token("user", "test-user");

        String creado = mockMvc.perform(post("/api/eventos/" + id + "/comentarios")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Primera opinión\",\"valoracion\":2}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long comentarioId = ((Number) JsonPath.read(creado, "$.id")).longValue();

        // Segundo comentario del mismo usuario: actualiza el anterior (200, no crea otro)
        mockMvc.perform(post("/api/eventos/" + id + "/comentarios")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Opinión corregida\",\"valoracion\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comentarioId))
                .andExpect(jsonPath("$.propio").value(true));

        mockMvc.perform(get("/api/eventos/" + id + "/comentarios").header("Authorization", token))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.promedio").value(5.0));

        mockMvc.perform(delete("/api/eventos/" + id + "/comentarios/" + comentarioId).header("Authorization", token))
                .andExpect(status().isNoContent());
        assertThat(comentarioRepository.findById(comentarioId)).isEmpty();
    }

    @Test
    void apiSoloElAutorOAdminEliminaUnComentario() throws Exception {
        long id = crearEvento();
        String creado = mockMvc.perform(post("/api/eventos/" + id + "/comentarios")
                        .header("Authorization", tokenOrganizador()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Comentario ajeno\",\"valoracion\":4}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long comentarioId = ((Number) JsonPath.read(creado, "$.id")).longValue();

        mockMvc.perform(delete("/api/eventos/" + id + "/comentarios/" + comentarioId)
                        .header("Authorization", token("user", "test-user")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/eventos/" + id + "/comentarios/" + comentarioId)
                        .header("Authorization", token("admin", "test-admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void apiRechazaComentariosInvalidos() throws Exception {
        String token = token("user", "test-user");
        mockMvc.perform(post("/api/eventos/1/comentarios")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"\",\"valoracion\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.texto").exists())
                .andExpect(jsonPath("$.errores.valoracion").exists());

        mockMvc.perform(post("/api/eventos/1/comentarios")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{json mal formado"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/eventos/999999/comentarios").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    // ===== API de compartir =====

    @Test
    void apiCompartirYDejarDeCompartir() throws Exception {
        long id = crearEvento();
        String token = token("user", "test-user");

        mockMvc.perform(get("/api/eventos/" + id + "/compartir").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(Matchers.endsWith("/eventos/" + id)))
                .andExpect(jsonPath("$.whatsapp").value(Matchers.startsWith("https://wa.me/?text=")))
                .andExpect(jsonPath("$.x").value(Matchers.startsWith("https://x.com/intent/post?text=")))
                .andExpect(jsonPath("$.compartidoPorMi").value(false));

        mockMvc.perform(post("/api/eventos/" + id + "/compartir").header("Authorization", token))
                .andExpect(jsonPath("$.compartidoPorMi").value(true))
                .andExpect(jsonPath("$.totalCompartidos").value(1));

        mockMvc.perform(delete("/api/eventos/" + id + "/compartir").header("Authorization", token))
                .andExpect(jsonPath("$.compartidoPorMi").value(false))
                .andExpect(jsonPath("$.totalCompartidos").value(0));
    }

    @Test
    void apiCompartirEventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/eventos/999999/compartir").header("Authorization", token("user", "test-user")))
                .andExpect(status().isNotFound());
    }

    // ===== Eliminar un evento con datos asociados =====

    @Test
    void eliminarEventoBorraSusComentariosFotosVideosYCompartidos() throws Exception {
        long id = crearEvento();
        String token = tokenOrganizador();
        mockMvc.perform(post("/api/eventos/" + id + "/comentarios").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\",\"valoracion\":3}"))
                .andExpect(status().isCreated());
        mockMvc.perform(multipart("/api/eventos/" + id + "/fotos").file(foto()).header("Authorization", token))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/eventos/" + id + "/videos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://youtu.be/dQw4w9WgXcQ\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/eventos/" + id + "/compartir").header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/eventos/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        assertThat(fotoRepository.countByEventoId(id)).isZero();
        assertThat(videoRepository.countByEventoId(id)).isZero();
    }

    // ===== Acciones del sitio web =====

    @Test
    void webOrganizadorEliminaFotosYVideos() throws Exception {
        long id = crearEvento();
        mockMvc.perform(multipart("/eventos/" + id + "/fotos").file(foto()).with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("mensajeMultimedia", "Foto agregada al evento."));
        mockMvc.perform(post("/eventos/" + id + "/videos").with(ORGANIZADOR).with(csrf())
                        .param("url", "https://youtu.be/dQw4w9WgXcQ"))
                .andExpect(flash().attribute("mensajeMultimedia", "Video agregado al evento."));

        long fotoId = fotoRepository.findIdsByEvento(id).get(0);
        long videoId = videoRepository.findByEventoIdOrderByIdAsc(id).get(0).getId();

        // El detalle muestra los botones de eliminar solo a organizadores/admin
        mockMvc.perform(get("/eventos/" + id).with(ORGANIZADOR))
                .andExpect(content().string(Matchers.containsString("Eliminar foto")))
                .andExpect(content().string(Matchers.containsString("Video del evento")));

        mockMvc.perform(post("/eventos/" + id + "/fotos/" + fotoId + "/eliminar").with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("mensajeMultimedia", "Foto eliminada."));
        mockMvc.perform(post("/eventos/" + id + "/videos/" + videoId + "/eliminar").with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("mensajeMultimedia", "Video eliminado."));
        assertThat(fotoRepository.countByEventoId(id)).isZero();
    }

    @Test
    void webRespetaElMaximoDeFotosYVideosPorEvento() throws Exception {
        long id = crearEvento();
        for (int i = 0; i < 12; i++) {
            mockMvc.perform(multipart("/eventos/" + id + "/fotos").file(foto()).with(ORGANIZADOR).with(csrf()))
                    .andExpect(flash().attribute("mensajeMultimedia", "Foto agregada al evento."));
        }
        mockMvc.perform(multipart("/eventos/" + id + "/fotos").file(foto()).with(ORGANIZADOR).with(csrf()))
                .andExpect(flash().attribute("errorMultimedia", "El evento ya tiene el máximo de 12 fotos"));

        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/eventos/" + id + "/videos").with(ORGANIZADOR).with(csrf())
                    .param("url", "https://youtu.be/dQw4w9WgXcQ"));
        }
        mockMvc.perform(post("/eventos/" + id + "/videos").with(ORGANIZADOR).with(csrf())
                        .param("url", "https://youtu.be/dQw4w9WgXcQ"))
                .andExpect(flash().attribute("errorMultimedia", "El evento ya tiene el máximo de 6 videos"));
    }

    @Test
    void webComentarioSeActualizaYSeElimina() throws Exception {
        long id = crearEvento();
        mockMvc.perform(post("/eventos/" + id + "/comentarios").with(USUARIO).with(csrf())
                        .param("texto", "Me gustó").param("valoracion", "4"))
                .andExpect(flash().attribute("mensajeComentario", "¡Gracias! Tu comentario fue publicado."));
        mockMvc.perform(post("/eventos/" + id + "/comentarios").with(USUARIO).with(csrf())
                        .param("texto", "Me encantó").param("valoracion", "5"))
                .andExpect(flash().attribute("mensajeComentario", "Tu comentario fue actualizado."));

        // El formulario aparece con el comentario del usuario para editarlo
        mockMvc.perform(get("/eventos/" + id).with(USUARIO))
                .andExpect(content().string(Matchers.containsString("Actualizar comentario")))
                .andExpect(model().attribute("estrellasPromedio", 5));

        long comentarioId = comentarioRepository.findByEventoIdAndUsuarioUsername(id, "user").orElseThrow().getId();
        mockMvc.perform(post("/eventos/" + id + "/comentarios/" + comentarioId + "/eliminar").with(USUARIO).with(csrf()))
                .andExpect(flash().attribute("mensajeComentario", "Comentario eliminado."));
        assertThat(comentarioRepository.findById(comentarioId)).isEmpty();
    }

    @Test
    void webAdminPuedeEliminarComentariosDeOtros() throws Exception {
        long id = crearEvento();
        mockMvc.perform(post("/eventos/" + id + "/comentarios").with(USUARIO).with(csrf())
                .param("texto", "Comentario a moderar").param("valoracion", "1"));
        long comentarioId = comentarioRepository.findByEventoIdAndUsuarioUsername(id, "user").orElseThrow().getId();

        mockMvc.perform(post("/eventos/" + id + "/comentarios/" + comentarioId + "/eliminar").with(ADMIN).with(csrf()))
                .andExpect(redirectedUrl("/eventos/" + id + "#comentarios"));
        assertThat(comentarioRepository.findById(comentarioId)).isEmpty();
    }

    @Test
    void webDejarDeCompartirQuitaElEventoDelPerfil() throws Exception {
        long id = crearEvento();
        mockMvc.perform(post("/eventos/" + id + "/compartir").with(USUARIO).with(csrf()));
        mockMvc.perform(get("/eventos/" + id).with(USUARIO))
                .andExpect(content().string(Matchers.containsString("Quitar de mi perfil")));

        mockMvc.perform(post("/eventos/" + id + "/compartir/quitar").with(USUARIO).with(csrf()))
                .andExpect(flash().attribute("mensajeCompartir", "El evento ya no aparece en tu perfil."));
        mockMvc.perform(get("/perfil").with(USUARIO))
                .andExpect(content().string(Matchers.not(Matchers.containsString("href=\"/eventos/" + id + "\""))));
    }

    @Test
    void webDetalleDeEventoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/eventos/999999").with(USUARIO))
                .andExpect(status().isNotFound());
    }

    // ===== Perfil y búsqueda (rutas modificadas) =====

    @Test
    void perfilSeGuardaYValidaLaListaBlanca() throws Exception {
        mockMvc.perform(post("/perfil").with(USUARIO).with(csrf())
                        .param("gamertag", "jugador_1").param("avatar", "dragon").param("juegos", "Valorant")
                        .param("notifEmail", "true"))
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("mensaje", "Perfil actualizado correctamente."));

        mockMvc.perform(post("/perfil").with(USUARIO).with(csrf())
                        .param("gamertag", "jugador_1").param("avatar", "<script>").param("juegos", "Juego falso"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil"))
                .andExpect(model().attributeHasFieldErrors("perfilForm", "avatar", "juegos"));
    }

    @Test
    void busquedaConFechasInvertidasMuestraError() throws Exception {
        mockMvc.perform(get("/eventos").param("desde", "2030-12-31").param("hasta", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorFechas"));
    }
}
