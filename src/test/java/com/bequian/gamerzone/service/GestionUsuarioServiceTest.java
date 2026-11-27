package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.ComentarioRepository;
import com.bequian.gamerzone.repository.CompartidoRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;
import com.bequian.gamerzone.repository.VideoEventoRepository;

/**
 * Pruebas unitarias de las reglas de gestión de usuarios. Se usan repositorios simulados (Mockito)
 * para probar casos difíciles de armar con la BD real, como "queda un solo administrador".
 */
class GestionUsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private ComentarioRepository comentarioRepository;
    private CompartidoRepository compartidoRepository;
    private FotoEventoRepository fotoRepository;
    private VideoEventoRepository videoRepository;
    private PasswordEncoder passwordEncoder;
    private GestionUsuarioService servicio;

    private final Usuario admin = new Usuario("admin", "hash-admin", "Administrador", Set.of("USER", "ADMIN"));
    private final Usuario otroAdmin = new Usuario("jefa@test.cl", "hash-jefa", "Jefa", Set.of("USER", "ADMIN"));
    private final Usuario jugador = new Usuario("jugador@test.cl", "hash-jugador", "Jugador", Set.of("USER"));

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        comentarioRepository = mock(ComentarioRepository.class);
        compartidoRepository = mock(CompartidoRepository.class);
        fotoRepository = mock(FotoEventoRepository.class);
        videoRepository = mock(VideoEventoRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        servicio = new GestionUsuarioService(usuarioRepository, comentarioRepository, compartidoRepository,
                fotoRepository, videoRepository, passwordEncoder);

        when(passwordEncoder.encode(anyString())).thenAnswer(i -> "bcrypt(" + i.getArgument(0) + ")");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otroAdmin));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(jugador));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(usuarioRepository.contarPorRol("ADMIN")).thenReturn(2L);
    }

    // ===== Consultas =====

    @Test
    void listarYObtener() {
        when(usuarioRepository.findAllByOrderByUsernameAsc()).thenReturn(List.of(admin, jugador));
        assertThat(servicio.listar()).containsExactly(admin, jugador);
        assertThat(servicio.obtener(3L)).isSameAs(jugador);
        assertThat(servicio.contarAdministradores()).isEqualTo(2);
    }

    @Test
    void obtenerUsuarioInexistenteDevuelve404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ===== Crear =====

    @Test
    void crearNormalizaElCorreoCifraLaContrasenaYAgregaUser() {
        Usuario creado = servicio.crear("  Ana  ", "  ANA@Test.CL ", "Clave1234", List.of("admin"));

        assertThat(creado.getUsername()).isEqualTo("ana@test.cl");
        assertThat(creado.getNombre()).isEqualTo("Ana");
        assertThat(creado.getPassword()).isEqualTo("bcrypt(Clave1234)");   // nunca en texto plano
        assertThat(creado.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void crearSinRolesDejaSoloUser() {
        assertThat(servicio.crear("Ana", "ana@test.cl", "Clave1234", null).getRoles()).containsExactly("USER");
    }

    @Test
    void crearRechazaCorreoDuplicado() {
        when(usuarioRepository.existsByUsername("jugador@test.cl")).thenReturn(true);
        assertThatThrownBy(() -> servicio.crear("Otro", "Jugador@test.cl", "Clave1234", List.of()))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Ya existe una cuenta con este correo");
        verify(usuarioRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "sin-arroba", "dos@@test.cl", "espacio @test.cl"})
    void crearRechazaCorreosInvalidos(String correo) {
        assertThatThrownBy(() -> servicio.crear("Ana", correo, "Clave1234", List.of()))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Ingresa un correo válido");
    }

    @Test
    void crearRechazaCorreoDemasiadoLargo() {
        String largo = "a".repeat(95) + "@test.cl";
        assertThatThrownBy(() -> servicio.crear("Ana", largo, "Clave1234", List.of()))
                .isInstanceOf(ValidacionException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"corta1", "solamenteletras", "12345678"})
    void crearRechazaContrasenasDebiles(String password) {
        assertThatThrownBy(() -> servicio.crear("Ana", "ana@test.cl", password, List.of()))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("al menos una letra y un número");
    }

    @Test
    void crearRechazaNombreVacioOLargoYRolesNoPermitidos() {
        assertThatThrownBy(() -> servicio.crear(" ", "ana@test.cl", "Clave1234", List.of()))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> servicio.crear(null, "ana@test.cl", "Clave1234", List.of()))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> servicio.crear("a".repeat(61), "ana@test.cl", "Clave1234", List.of()))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> servicio.crear("Ana", "ana@test.cl", "Clave1234", List.of("ROOT")))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Rol no permitido");
        assertThatThrownBy(() -> servicio.crear("Ana", "ana@test.cl", "Clave1234", Arrays.asList((String) null)))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Rol no permitido");
    }

    // ===== Actualizar =====

    @Test
    void actualizarCambiaNombreRolesYContrasena() {
        Usuario actualizado = servicio.actualizar(3L, "Jugador Pro", List.of("ORGANIZADOR"), "NuevaClave1", "admin");

        assertThat(actualizado.getNombre()).isEqualTo("Jugador Pro");
        assertThat(actualizado.getRoles()).containsExactlyInAnyOrder("USER", "ORGANIZADOR");
        assertThat(actualizado.getPassword()).isEqualTo("bcrypt(NuevaClave1)");
    }

    @Test
    void actualizarSinContrasenaMantieneLaAnterior() {
        servicio.actualizar(3L, "Jugador", List.of(), "   ", "admin");
        assertThat(jugador.getPassword()).isEqualTo("hash-jugador");
        servicio.actualizar(3L, "Jugador", List.of(), null, "admin");
        assertThat(jugador.getPassword()).isEqualTo("hash-jugador");
    }

    @Test
    void actualizarRechazaContrasenaDebil() {
        assertThatThrownBy(() -> servicio.actualizar(3L, "Jugador", List.of(), "debil", "admin"))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void adminNoPuedeQuitarseSuPropioRol() {
        assertThatThrownBy(() -> servicio.actualizar(1L, "Administrador", List.of("USER"), null, "ADMIN"))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("No puedes quitarte tu propio rol de administrador");
    }

    @Test
    void siempreDebeQuedarUnAdministrador() {
        when(usuarioRepository.contarPorRol("ADMIN")).thenReturn(1L);
        assertThatThrownBy(() -> servicio.actualizar(2L, "Jefa", List.of(), null, "admin"))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Debe quedar al menos un administrador");
    }

    @Test
    void conVariosAdministradoresSePuedeQuitarElRolAOtro() {
        assertThat(servicio.actualizar(2L, "Jefa", List.of(), null, "admin").getRoles()).containsExactly("USER");
    }

    // ===== Eliminar =====

    @Test
    void eliminarBorraComentariosYCompartidosYReasignaMultimediaAlAdmin() {
        servicio.eliminar(3L, "admin");

        InOrder orden = inOrder(comentarioRepository, compartidoRepository, fotoRepository, videoRepository,
                usuarioRepository);
        orden.verify(comentarioRepository).deleteByUsuario(jugador.getId());
        orden.verify(compartidoRepository).deleteByUsuario(jugador.getId());
        orden.verify(fotoRepository).reasignarAutor(jugador.getId(), admin);
        orden.verify(videoRepository).reasignarAutor(jugador.getId(), admin);
        orden.verify(usuarioRepository).delete(jugador);
    }

    @Test
    void eliminarSeRechazaParaLaPropiaCuentaYElUltimoAdmin() {
        assertThatThrownBy(() -> servicio.eliminar(1L, "admin"))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("No puedes eliminar tu propia cuenta");

        when(usuarioRepository.contarPorRol("ADMIN")).thenReturn(1L);
        assertThatThrownBy(() -> servicio.eliminar(2L, "admin"))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Debe quedar al menos un administrador");
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void eliminarConUnActorQueNoExisteSeRechaza() {
        assertThatThrownBy(() -> servicio.eliminar(3L, "fantasma"))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("Administrador no válido");
        verify(usuarioRepository, never()).delete(any());
    }

    // ===== Utilidades =====

    @Test
    void rolesOrdenadosYValidacionDeContrasena() {
        assertThat(GestionUsuarioService.rolesOrdenados(admin)).containsExactly("ADMIN", "USER");
        assertThat(GestionUsuarioService.passwordValida("Clave1234")).isTrue();
        assertThat(GestionUsuarioService.passwordValida(null)).isFalse();
        assertThat(GestionUsuarioService.passwordValida("a1".repeat(37))).isFalse();   // más de 72
        assertThat(GestionUsuarioService.ROLES).containsExactly("USER", "ORGANIZADOR", "ADMIN");
    }
}
