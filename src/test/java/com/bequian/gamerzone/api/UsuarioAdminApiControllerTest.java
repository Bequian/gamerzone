package com.bequian.gamerzone.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bequian.gamerzone.DatosPrueba;
import com.bequian.gamerzone.repository.ComentarioRepository;
import com.bequian.gamerzone.repository.CompartidoRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;
import com.bequian.gamerzone.service.EventoService;

/**
 * [Privada - ADMIN] API de gestión de usuarios: control de acceso, validaciones y reglas de negocio.
 * Se usan tokens JWT reales para probar la cadena de seguridad completa de la API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioAdminApiControllerTest {

    private static final String RUTA = "/api/admin/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private CompartidoRepository compartidoRepository;

    @Autowired
    private FotoEventoRepository fotoRepository;

    @Autowired
    private EventoService eventoService;

    private String admin() throws Exception {
        return DatosPrueba.bearer(mockMvc, "admin", "test-admin");
    }

    private long crearUsuario(String token, String correo, String roles) throws Exception {
        String json = mockMvc.perform(post(RUTA).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Usuario API", "correo": "%s", "password": "%s", "roles": %s}
                                """.formatted(correo, DatosPrueba.PASSWORD_VALIDA, roles)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return DatosPrueba.idDe(json);
    }

    // ===== Control de acceso =====

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get(RUTA)).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioSinRolAdminRecibe403() throws Exception {
        mockMvc.perform(get(RUTA).header("Authorization", DatosPrueba.bearer(mockMvc, "user", "test-user")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(RUTA).header("Authorization", DatosPrueba.bearer(mockMvc, "organizador", "test-org")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListaUsuariosSinExponerContrasenas() throws Exception {
        mockMvc.perform(get(RUTA).header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username").value(Matchers.hasItem("admin")))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    // ===== CRUD =====

    @Test
    void adminCreaConsultaEditaYEliminaUnUsuario() throws Exception {
        String token = admin();
        String correo = DatosPrueba.correoUnico("api");
        long id = crearUsuario(token, correo.toUpperCase(), "[\"ORGANIZADOR\"]");

        // El correo se guarda en minúsculas y USER se agrega siempre
        mockMvc.perform(get(RUTA + "/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(correo))
                .andExpect(jsonPath("$.roles").value(Matchers.contains("ORGANIZADOR", "USER")));

        mockMvc.perform(put(RUTA + "/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo nombre\",\"roles\":[\"ADMIN\"],\"password\":\"OtraClave99\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo nombre"))
                .andExpect(jsonPath("$.roles").value(Matchers.contains("ADMIN", "USER")));

        // La nueva contraseña funciona y el nuevo admin puede usar la API de administración
        String tokenNuevo = DatosPrueba.bearer(mockMvc, correo, "OtraClave99");
        mockMvc.perform(get(RUTA).header("Authorization", tokenNuevo)).andExpect(status().isOk());

        mockMvc.perform(delete(RUTA + "/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(RUTA + "/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
        // El token del usuario eliminado deja de servir
        mockMvc.perform(get("/api/eventos/1").header("Authorization", tokenNuevo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminarUsuarioBorraSusComentariosYConservaSusFotos() throws Exception {
        String token = admin();
        String correo = DatosPrueba.correoUnico("org");
        long id = crearUsuario(token, correo, "[\"ORGANIZADOR\"]");
        long eventoId = eventoService.crear(DatosPrueba.eventoRequest("Evento usuario eliminado")).getId();
        String tokenOrg = DatosPrueba.bearer(mockMvc, correo, DatosPrueba.PASSWORD_VALIDA);

        mockMvc.perform(post("/api/eventos/" + eventoId + "/comentarios").header("Authorization", tokenOrg)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Hola\",\"valoracion\":5}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/eventos/" + eventoId + "/compartir").header("Authorization", tokenOrg))
                .andExpect(status().isOk());
        long fotoId = DatosPrueba.idDe(mockMvc.perform(multipart("/api/eventos/" + eventoId + "/fotos")
                        .file(DatosPrueba.fotoPng()).header("Authorization", tokenOrg))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(post("/api/eventos/" + eventoId + "/videos").header("Authorization", tokenOrg)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://youtu.be/dQw4w9WgXcQ\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete(RUTA + "/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        assertThat(usuarioRepository.findById(id)).isEmpty();
        assertThat(comentarioRepository.findByEventoIdAndUsuarioUsername(eventoId, correo)).isEmpty();
        assertThat(compartidoRepository.countByEventoId(eventoId)).isZero();
        assertThat(fotoRepository.findById(fotoId)).isPresent();   // la foto queda a nombre del admin
    }

    // ===== Validaciones y reglas de negocio =====

    @Test
    void datosInvalidosDevuelven400ConDetalle() throws Exception {
        mockMvc.perform(post(RUTA).header("Authorization", admin()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"correo\":\"no-es-correo\",\"password\":\"corta\",\"roles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.password").exists());
    }

    @Test
    void rolNoPermitidoYCorreoDuplicadoSeRechazan() throws Exception {
        String token = admin();
        mockMvc.perform(post(RUTA).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\",\"correo\":\"" + DatosPrueba.correoUnico("rol")
                                + "\",\"password\":\"Clave1234\",\"roles\":[\"SUPERADMIN\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Rol no permitido"));

        String correo = DatosPrueba.correoUnico("dup");
        crearUsuario(token, correo, "[]");
        mockMvc.perform(post(RUTA).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\",\"correo\":\"" + correo
                                + "\",\"password\":\"Clave1234\",\"roles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuenta con este correo"));
    }

    @Test
    void adminNoPuedeQuitarseSuRolNiEliminarse() throws Exception {
        String token = admin();
        long idAdmin = usuarioRepository.findByUsername("admin").orElseThrow().getId();

        mockMvc.perform(put(RUTA + "/" + idAdmin).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Administrador\",\"roles\":[\"USER\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No puedes quitarte tu propio rol de administrador"));

        mockMvc.perform(delete(RUTA + "/" + idAdmin).header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No puedes eliminar tu propia cuenta"));
    }

    @Test
    void usuarioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(put(RUTA + "/999999").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\",\"roles\":[]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Usuario no encontrado"));
    }
}
