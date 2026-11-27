package com.bequian.gamerzone.api.dto;

import java.util.List;

import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.service.GestionUsuarioService;

// Lo que la API devuelve de un usuario: NUNCA incluye el hash de la contraseña
public record UsuarioResponse(Long id, String username, String nombre, List<String> roles) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getNombre(),
                GestionUsuarioService.rolesOrdenados(usuario));
    }
}
