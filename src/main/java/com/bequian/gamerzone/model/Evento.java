package com.bequian.gamerzone.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

// Entidad JPA: cada objeto Evento es una fila de la tabla "evento"
@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT en MySQL
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 40)
    private String categoria;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 60)
    private String ubicacion;

    @Column(nullable = false, length = 200)
    private String direccion;

    // Listas de textos: se guardan en tablas aparte relacionadas por evento_id
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evento_organizador", joinColumns = @JoinColumn(name = "evento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "nombre", nullable = false, length = 100)
    private List<String> organizadores = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evento_expositor", joinColumns = @JoinColumn(name = "evento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "nombre", nullable = false, length = 100)
    private List<String> expositores = new ArrayList<>();

    @Embedded
    private ServiciosEvento servicios;

    private boolean destacado;

    // JPA necesita un constructor sin argumentos
    protected Evento() {
    }

    public Evento(String titulo, String categoria, String descripcion,
                  LocalDate fecha, LocalTime hora, String ubicacion, String direccion,
                  List<String> organizadores, ServiciosEvento servicios,
                  List<String> expositores, boolean destacado) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.organizadores = new ArrayList<>(organizadores);
        this.servicios = servicios;
        this.expositores = new ArrayList<>(expositores);
        this.destacado = destacado;
    }

        // Reemplaza los datos del evento (lo usa la API de gestión de eventos)
    public void actualizar(String titulo, String categoria, String descripcion,
                           LocalDate fecha, LocalTime hora, String ubicacion, String direccion,
                           List<String> organizadores, ServiciosEvento servicios,
                           List<String> expositores, boolean destacado) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.organizadores.clear();
        this.organizadores.addAll(organizadores);
        this.servicios = servicios;
        this.expositores.clear();
        this.expositores.addAll(expositores);
        this.destacado = destacado;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getUbicacion() { return ubicacion; }
    public String getDireccion() { return direccion; }
    public List<String> getOrganizadores() { return organizadores; }
    public ServiciosEvento getServicios() { return servicios; }
    public List<String> getExpositores() { return expositores; }
    public boolean isDestacado() { return destacado; }
}