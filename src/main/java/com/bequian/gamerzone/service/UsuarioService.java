package com.bequian.gamerzone.service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.bequian.gamerzone.model.Perfil;
import com.bequian.gamerzone.model.RegistroForm;

@Service
public class UsuarioService {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    // Sin base de datos: estos datos se pierden al reiniciar la aplicación

    private final Map<String, String> nombres = new ConcurrentHashMap<>();
    private final Map<String, Perfil> perfiles = new ConcurrentHashMap<>();

    public UsuarioService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registro de nuevo usuario, el correo se usa como nombre de usuario para el login.
     * Retorna un boolean que indica si el registro fue exitoso (true) o si el correo ya estaba registrado (false).
     * Sincronizado para evitar correos duplicados.
     */
    public synchronized boolean registrar(RegistroForm form) {
        String correo = normalizar(form.getCorreo());
        if (userDetailsManager.userExists(correo)) {
            return false;
        }

        UserDetails nuevo = User.builder()
                .username(correo)
                .password(passwordEncoder.encode(form.getPassword()))
                .roles("USER")   // El rol lo decide el servidor, nunca el formulario
                .build();

        userDetailsManager.createUser(nuevo);
        nombres.put(correo, form.getNombre().trim());
        return true;
    }

    public String nombreDe(String username) {
        return nombres.getOrDefault(normalizar(username), username);
    }

    public Perfil obtenerPerfil(String username) {
        return perfiles.getOrDefault(normalizar(username), Perfil.porDefecto());
    }

    public void guardarPerfil(String username, Perfil perfil) {
        perfiles.put(normalizar(username), perfil);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }
}