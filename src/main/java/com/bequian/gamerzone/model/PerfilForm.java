package com.bequian.gamerzone.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos obtenidos del formulario de edición de perfil, con validaciones
public class PerfilForm {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 20, message = "Debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Solo letras, números y guion bajo")
    private String gamertag;

    @NotBlank(message = "Selecciona un avatar")
    private String avatar;

    @Size(max = 5, message = "Puedes elegir hasta 5 juegos")
    private List<String> juegos = new ArrayList<>();

    private boolean notifEmail;
    private boolean notifNuevosEventos;
    private boolean notifRecordatorios;

    // Convierte el perfil guardado en un formulario para editarlo
    public static PerfilForm desde(Perfil perfil) {
        PerfilForm form = new PerfilForm();
        form.setGamertag(perfil.gamertag());
        form.setAvatar(perfil.avatar());
        form.setJuegos(new ArrayList<>(perfil.juegos()));
        form.setNotifEmail(perfil.notifEmail());
        form.setNotifNuevosEventos(perfil.notifNuevosEventos());
        form.setNotifRecordatorios(perfil.notifRecordatorios());
        return form;
    }

    // Convierte el formulario en un perfil inmutable para guardarlo
    public Perfil aPerfil() {
        return new Perfil(gamertag.trim(), avatar, List.copyOf(getJuegos()),
                notifEmail, notifNuevosEventos, notifRecordatorios);
    }

    public String getGamertag() { return gamertag; }
    public void setGamertag(String gamertag) { this.gamertag = gamertag; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<String> getJuegos() { return juegos == null ? new ArrayList<>() : juegos; }
    public void setJuegos(List<String> juegos) { this.juegos = juegos; }

    public boolean isNotifEmail() { return notifEmail; }
    public void setNotifEmail(boolean notifEmail) { this.notifEmail = notifEmail; }

    public boolean isNotifNuevosEventos() { return notifNuevosEventos; }
    public void setNotifNuevosEventos(boolean notifNuevosEventos) { this.notifNuevosEventos = notifNuevosEventos; }

    public boolean isNotifRecordatorios() { return notifRecordatorios; }
    public void setNotifRecordatorios(boolean notifRecordatorios) { this.notifRecordatorios = notifRecordatorios; }
}