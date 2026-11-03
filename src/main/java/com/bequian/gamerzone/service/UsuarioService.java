package com.bequian.gamerzone.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bequian.gamerzone.model.Perfil;
import com.bequian.gamerzone.model.RegistroForm;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un usuario nuevo en la BD. El correo se usa como nombre de usuario para el login.
     * Devuelve false si el correo ya estaba registrado.
     */
    @Transactional
    public synchronized boolean registrar(RegistroForm form) {
        String correo = normalizar(form.getCorreo());
        if (usuarioRepository.existsByUsername(correo)) {
            return false;
        }

        Usuario nuevo = new Usuario(
                correo,
                passwordEncoder.encode(form.getPassword()),
                form.getNombre().trim(),
                Set.of("USER"));   // El rol lo decide el servidor, nunca el formulario

        usuarioRepository.save(nuevo);
        return true;
    }

    @Transactional(readOnly = true)
    public String nombreDe(String username) {
        return usuarioRepository.findByUsername(normalizar(username))
                .map(Usuario::getNombre)
                .orElse(username);
    }

    @Transactional(readOnly = true)
    public Perfil obtenerPerfil(String username) {
        return usuarioRepository.findByUsername(normalizar(username))
                .map(Usuario::getPerfil)
                .orElse(Perfil.porDefecto());
    }

    @Transactional
    public void guardarPerfil(String username, Perfil perfil) {
        usuarioRepository.findByUsername(normalizar(username)).ifPresent(usuario -> {
            usuario.actualizarPerfil(perfil);
            usuarioRepository.save(usuario);
        });
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }
}