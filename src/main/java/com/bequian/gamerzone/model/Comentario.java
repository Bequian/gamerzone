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

// Comentario y valoración (1 a 5 estrellas) de un usuario sobre un evento.
// La restricción UNIQUE asegura un solo comentario por usuario y evento (no se puede inflar la nota).
@Entity
@Table(name = "comentario",
       uniqueConstraints = @UniqueConstraint(name = "uk_comentario_evento_usuario",
                                             columnNames = {"evento_id", "usuario_id"}))
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false)
    private int valoracion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Comentario() {
    }

    public Comentario(Evento evento, Usuario usuario, String texto, int valoracion) {
        this.evento = evento;
        this.usuario = usuario;
        this.texto = texto;
        this.valoracion = valoracion;
        this.fecha = LocalDateTime.now();
    }

    // Si el usuario vuelve a comentar el mismo evento, se actualiza su comentario
    public void actualizar(String texto, int valoracion) {
        this.texto = texto;
        this.valoracion = valoracion;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public Usuario getUsuario() { return usuario; }
    public String getTexto() { return texto; }
    public int getValoracion() { return valoracion; }
    public LocalDateTime getFecha() { return fecha; }
}
