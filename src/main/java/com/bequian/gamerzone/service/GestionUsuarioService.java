package com.bequian.gamerzone.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.ComentarioRepository;
import com.bequian.gamerzone.repository.CompartidoRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;
import com.bequian.gamerzone.repository.VideoEventoRepository;

/**
 * [Privada - ADMIN] Gestión de los usuarios del sitio y de los administradores.
 * Todas las reglas se validan aquí (en el servidor), tanto para la web como para la API.
 */
@Service
public class GestionUsuarioService {

    public static final String ROL_USER = "USER";
    public static final String ROL_ORGANIZADOR = "ORGANIZADOR";
    public static final String ROL_ADMIN = "ADMIN";

    // Lista blanca: solo estos roles se pueden asignar
    public static final List<String> ROLES = List.of(ROL_USER, ROL_ORGANIZADOR, ROL_ADMIN);

    // Misma política de contraseñas que el registro
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final CompartidoRepository compartidoRepository;
    private final FotoEventoRepository fotoEventoRepository;
    private final VideoEventoRepository videoEventoRepository;
    private final PasswordEncoder passwordEncoder;

    public GestionUsuarioService(UsuarioRepository usuarioRepository,
                                 ComentarioRepository comentarioRepository,
                                 CompartidoRepository compartidoRepository,
                                 FotoEventoRepository fotoEventoRepository,
                                 VideoEventoRepository videoEventoRepository,
                                 PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.compartidoRepository = compartidoRepository;
        this.fotoEventoRepository = fotoEventoRepository;
        this.videoEventoRepository = videoEventoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public long contarAdministradores() {
        return usuarioRepository.contarPorRol(ROL_ADMIN);
    }

    /** Crea un usuario con los roles indicados. El correo se usa como nombre de usuario. */
    @Transactional
    public synchronized Usuario crear(String nombre, String correo, String password, List<String> roles) {
        String username = normalizar(correo);
        if (!CORREO.matcher(username).matches() || username.length() > 100) {
            throw new ValidacionException("Ingresa un correo válido");
        }
        validarNombre(nombre);
        validarPassword(password);
        Set<String> rolesFinales = normalizarRoles(roles);

        if (usuarioRepository.existsByUsername(username)) {
            throw new ValidacionException("Ya existe una cuenta con este correo");
        }
        return usuarioRepository.save(
                new Usuario(username, passwordEncoder.encode(password), nombre.trim(), rolesFinales));
    }

    /**
     * Actualiza nombre y roles; si nuevaPassword viene con texto, también cambia la contraseña.
     * "actor" es el username del administrador que hace el cambio.
     */
    @Transactional
    public synchronized Usuario actualizar(Long id, String nombre, List<String> roles,
                                           String nuevaPassword, String actor) {
        Usuario usuario = obtener(id);
        validarNombre(nombre);
        Set<String> rolesFinales = normalizarRoles(roles);

        boolean eraAdmin = usuario.getRoles().contains(ROL_ADMIN);
        boolean seraAdmin = rolesFinales.contains(ROL_ADMIN);
        if (eraAdmin && !seraAdmin) {
            if (usuario.getUsername().equalsIgnoreCase(actor)) {
                throw new ValidacionException("No puedes quitarte tu propio rol de administrador");
            }
            if (contarAdministradores() <= 1) {
                throw new ValidacionException("Debe quedar al menos un administrador");
            }
        }

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            validarPassword(nuevaPassword);
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }
        usuario.setNombre(nombre.trim());
        usuario.setRoles(rolesFinales);
        return usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario. Sus comentarios y compartidos se eliminan; sus fotos y videos
     * quedan a nombre del administrador que lo elimina, para no perder el contenido del evento.
     */
    @Transactional
    public synchronized void eliminar(Long id, String actor) {
        Usuario usuario = obtener(id);
        if (usuario.getUsername().equalsIgnoreCase(actor)) {
            throw new ValidacionException("No puedes eliminar tu propia cuenta");
        }
        if (usuario.getRoles().contains(ROL_ADMIN) && contarAdministradores() <= 1) {
            throw new ValidacionException("Debe quedar al menos un administrador");
        }
        Usuario admin = usuarioRepository.findByUsername(normalizar(actor))
                .orElseThrow(() -> new ValidacionException("Administrador no válido"));

        comentarioRepository.deleteByUsuario(usuario.getId());
        compartidoRepository.deleteByUsuario(usuario.getId());
        fotoEventoRepository.reasignarAutor(usuario.getId(), admin);
        videoEventoRepository.reasignarAutor(usuario.getId(), admin);
        usuarioRepository.delete(usuario);
    }

    /** Roles ordenados para mostrarlos (vista y API). */
    public static List<String> rolesOrdenados(Usuario usuario) {
        return new ArrayList<>(new TreeSet<>(usuario.getRoles()));
    }

    public static boolean passwordValida(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }

    // USER siempre se incluye; cualquier rol fuera de la lista blanca se rechaza
    Set<String> normalizarRoles(List<String> roles) {
        Set<String> resultado = new TreeSet<>();
        resultado.add(ROL_USER);
        if (roles != null) {
            for (String rol : roles) {
                String limpio = rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
                if (!ROLES.contains(limpio)) {
                    throw new ValidacionException("Rol no permitido");
                }
                resultado.add(limpio);
            }
        }
        return resultado;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 60) {
            throw new ValidacionException("El nombre es obligatorio (máximo 60 caracteres)");
        }
    }

    private void validarPassword(String password) {
        if (!passwordValida(password)) {
            throw new ValidacionException("La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número");
        }
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }
}
