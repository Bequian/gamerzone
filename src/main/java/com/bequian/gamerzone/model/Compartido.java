package com.bequian.gamerzone.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Registro de que un usuario compartió un evento dentro de GamerZone.
// Aparece en su perfil ("Eventos que compartiste") y suma al contador del evento.
@Entity
@Table(name = "compartido",
       uniqueConstraints = @UniqueConstraint(name = "uk_compartido_evento_usuario",
                                             columnNames = {"evento_id", "usuario_id"}))
public class Compartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Compartido() {
    }

    public Compartido(Evento evento, Usuario usuario) {
        this.evento = evento;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFecha() { return fecha; }
}
