package com.bequian.gamerzone.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Foto de un evento. La imagen se guarda en la BD (no en el disco del servidor),
// así no se puede subir un archivo que luego se ejecute o se sirva desde otra ruta.
@Entity
@Table(name = "foto_evento")
public class FotoEvento {

    public static final int TAMANO_MAXIMO = 2 * 1024 * 1024;   // 2 MB

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario subidaPor;

    // Tipo validado por el servidor ("image/jpeg" o "image/png"), nunca el que envía el navegador
    @Column(nullable = false, length = 20)
    private String tipoContenido;

    @Lob
    @Column(nullable = false, length = TAMANO_MAXIMO)
    private byte[] datos;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected FotoEvento() {
    }

    public FotoEvento(Evento evento, Usuario subidaPor, String tipoContenido, byte[] datos) {
        this.evento = evento;
        this.subidaPor = subidaPor;
        this.tipoContenido = tipoContenido;
        this.datos = datos;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Evento getEvento() { return evento; }
    public String getTipoContenido() { return tipoContenido; }
    public byte[] getDatos() { return datos; }
    public LocalDateTime getFecha() { return fecha; }
}
