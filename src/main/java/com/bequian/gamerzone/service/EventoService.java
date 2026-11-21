package com.bequian.gamerzone.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.repository.EventoRepository;

import org.springframework.transaction.annotation.Transactional;
import com.bequian.gamerzone.api.dto.EventoRequest;
import com.bequian.gamerzone.repository.ComentarioRepository;
import com.bequian.gamerzone.repository.CompartidoRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.bequian.gamerzone.repository.VideoEventoRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final ComentarioRepository comentarioRepository;
    private final FotoEventoRepository fotoRepository;
    private final VideoEventoRepository videoRepository;
    private final CompartidoRepository compartidoRepository;

    public EventoService(EventoRepository eventoRepository, ComentarioRepository comentarioRepository,
                         FotoEventoRepository fotoRepository, VideoEventoRepository videoRepository,
                         CompartidoRepository compartidoRepository) {
        this.eventoRepository = eventoRepository;
        this.comentarioRepository = comentarioRepository;
        this.fotoRepository = fotoRepository;
        this.videoRepository = videoRepository;
        this.compartidoRepository = compartidoRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAllByOrderByFechaAscHoraAsc();
    }

    // Eventos desde hoy en adelante, limitados a una cantidad
    public List<Evento> listarProximos(int cantidad) {
        LocalDate hoy = LocalDate.now();
        return listarTodos().stream()
                .filter(e -> !e.getFecha().isBefore(hoy))
                .limit(cantidad)
                .toList();
    }

    public List<Evento> listarDestacados() {
        return eventoRepository.findByDestacadoTrueOrderByFechaAscHoraAsc();
    }

    public Optional<Evento> buscarPorId(Long id) {
        return eventoRepository.findById(id);
    }

    // Filtros opcionales: si un filtro viene vacío, no se aplica
    public List<Evento> buscar(String categoria, String ubicacion, LocalDate desde, LocalDate hasta) {
        return listarTodos().stream()
                .filter(e -> !StringUtils.hasText(categoria) || e.getCategoria().equals(categoria))
                .filter(e -> !StringUtils.hasText(ubicacion) || e.getUbicacion().equals(ubicacion))
                .filter(e -> desde == null || !e.getFecha().isBefore(desde))
                .filter(e -> hasta == null || !e.getFecha().isAfter(hasta))
                .toList();
    }

    public List<String> categorias() {
        return listarTodos().stream().map(Evento::getCategoria).distinct().sorted().toList();
    }

    public List<String> ubicaciones() {
        return listarTodos().stream().map(Evento::getUbicacion).distinct().sorted().toList();
    }

        // ===== Gestión de eventos (API, roles ORGANIZADOR y ADMIN) =====

    @Transactional
    public Evento crear(EventoRequest r) {
        Evento evento = new Evento(r.titulo().trim(), r.categoria().trim(), r.descripcion().trim(),
                r.fecha(), r.hora(), r.ubicacion().trim(), r.direccion().trim(),
                r.organizadores(), r.servicios(), r.expositores(), r.destacado());
        return eventoRepository.save(evento);
    }

    @Transactional
    public Optional<Evento> actualizar(Long id, EventoRequest r) {
        return eventoRepository.findById(id).map(evento -> {
            evento.actualizar(r.titulo().trim(), r.categoria().trim(), r.descripcion().trim(),
                    r.fecha(), r.hora(), r.ubicacion().trim(), r.direccion().trim(),
                    r.organizadores(), r.servicios(), r.expositores(), r.destacado());
            return eventoRepository.save(evento);
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!eventoRepository.existsById(id)) {
            return false;
        }
        // Primero los datos asociados al evento (claves foráneas), luego el evento
        comentarioRepository.deleteByEvento(id);
        fotoRepository.deleteByEvento(id);
        videoRepository.deleteByEvento(id);
        compartidoRepository.deleteByEvento(id);
        eventoRepository.deleteById(id);
        return true;
    }
}