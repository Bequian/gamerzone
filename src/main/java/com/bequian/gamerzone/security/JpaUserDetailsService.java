package com.bequian.gamerzone.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.repository.UsuarioRepository;

// Spring Security usa esta clase para buscar al usuario en MySQL cuando alguien inicia sesión
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public JpaUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizado = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByUsername(normalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())   // hash BCrypt guardado en la BD
                .roles(usuario.getRoles().toArray(String[]::new))
                .build();
    }
}