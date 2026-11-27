package com.bequian.gamerzone.model;

import java.util.ArrayList;
import java.util.List;

// Datos del formulario de crear/editar usuario del panel de administración.
// Las validaciones las hace GestionUsuarioService (igual para la web y la API).
public class UsuarioForm {

    private String nombre;
    private String correo;
    private String password;
    private List<String> roles = new ArrayList<>();

    public static UsuarioForm desde(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.setNombre(usuario.getNombre());
        form.setCorreo(usuario.getUsername());
        form.setRoles(new ArrayList<>(usuario.getRoles()));
        return form;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles == null ? new ArrayList<>() : roles; }
}
