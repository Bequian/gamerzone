package com.bequian.gamerzone.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

// Entidad JPA: cada objeto Usuario es una fila de la tabla "usuario"
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de usuario para el login (los registrados usan su correo)
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // Hash BCrypt, NUNCA la contraseña en texto plano
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 60)
    private String nombre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "rol", nullable = false, length = 30)
    private Set<String> roles = new HashSet<>();

    // ===== Datos del perfil =====
    @Column(length = 20)
    private String gamertag = "";

    @Column(nullable = false, length = 20)
    private String avatar = "control";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_juego", joinColumns = @JoinColumn(name = "usuario_id"))
    @OrderColumn(name = "orden")
    @Column(name = "juego", nullable = false, length = 40)
    private List<String> juegos = new ArrayList<>();

    private boolean notifEmail = true;
    private boolean notifNuevosEventos = true;
    private boolean notifRecordatorios = false;

    protected Usuario() {
    }

    public Usuario(String username, String passwordHash, String nombre, Set<String> roles) {
        this.username = username;
        this.password = passwordHash;
        this.nombre = nombre;
        this.roles = new HashSet<>(roles);
    }

    // Devuelve el perfil como objeto inmutable (lo usan PerfilController y las vistas)
    public Perfil getPerfil() {
        return new Perfil(gamertag == null ? "" : gamertag, avatar, List.copyOf(juegos),
                notifEmail, notifNuevosEventos, notifRecordatorios);
    }

    public void actualizarPerfil(Perfil perfil) {
        this.gamertag = perfil.gamertag();
        this.avatar = perfil.avatar();
        this.juegos.clear();
        this.juegos.addAll(perfil.juegos());
        this.notifEmail = perfil.notifEmail();
        this.notifNuevosEventos = perfil.notifNuevosEventos();
        this.notifRecordatorios = perfil.notifRecordatorios();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNombre() { return nombre; }
    public Set<String> getRoles() { return roles; }

    public void setPassword(String passwordHash) { this.password = passwordHash; }

    public void setRoles(Set<String> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }
}