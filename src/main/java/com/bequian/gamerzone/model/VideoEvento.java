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

// Video de YouTube asociado a un evento. Solo se guarda el ID del video (11 caracteres validados),
// nunca la URL completa ni código HTML: así no se puede inyectar contenido en la página.
@Entity
@Table(name = "video_evento")
public class VideoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario agregadoPor;

    @Column(nullable = false, length = 11)
    private String youtubeId;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected VideoEvento() {
    }

    public VideoEvento(Evento evento, Usuario agregadoPor, String youtubeId, String titulo) {
        this.evento = evento;
        this.agregadoPor = agregadoPor;
        this.youtubeId = youtubeId;
        this.titulo = titulo;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public String getYoutubeId() { return youtubeId; }
    public String getTitulo() { return titulo; }
    public LocalDateTime getFecha() { return fecha; }
}
