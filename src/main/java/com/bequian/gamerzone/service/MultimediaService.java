package com.bequian.gamerzone.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.api.dto.VideoResponse;
import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.FotoEvento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.model.VideoEvento;
import com.bequian.gamerzone.repository.EventoRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;
import com.bequian.gamerzone.repository.VideoEventoRepository;

/**
 * Fotos y videos de los eventos.
 *
 * Medidas de seguridad en la subida de imágenes (OWASP: Unrestricted File Upload):
 *  - Tamaño máximo de 2 MB y dimensiones máximas de 6000 x 6000 px (evita "bombas" de descompresión).
 *  - El tipo se detecta por la firma del archivo (magic bytes), no por la extensión ni por el
 *    Content-Type que envía el navegador, que el atacante puede falsificar.
 *  - La imagen se decodifica y se vuelve a codificar: si no es una imagen real, falla; y al
 *    re-codificarla se descartan metadatos (por ejemplo la ubicación GPS de la foto) y cualquier
 *    contenido extra escondido dentro del archivo.
 *  - Nunca se usa el nombre original del archivo, y la imagen se guarda en la BD, no en disco.
 */
@Service
public class MultimediaService {

    public static final int MAX_FOTOS_POR_EVENTO = 12;
    public static final int MAX_VIDEOS_POR_EVENTO = 6;
    private static final int MAX_DIMENSION = 6000;

    // Enlaces de YouTube aceptados: youtube.com/watch?v=ID, youtu.be/ID, /shorts/ID, /embed/ID, /live/ID
    private static final Pattern YOUTUBE = Pattern.compile(
            "^(?:https?://)?(?:www\\.|m\\.)?"
            + "(?:youtube\\.com/(?:watch\\?(?:[^#\\s]*&)?v=|embed/|shorts/|live/)|youtu\\.be/)"
            + "([A-Za-z0-9_-]{11})(?:[?&#/][^\\s]*)?$");

    // Imagen ya validada, lista para guardar o descargar
    public record Imagen(String tipoContenido, byte[] datos) {
    }

    private final FotoEventoRepository fotoRepository;
    private final VideoEventoRepository videoRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public MultimediaService(FotoEventoRepository fotoRepository, VideoEventoRepository videoRepository,
                             EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.fotoRepository = fotoRepository;
        this.videoRepository = videoRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ===== Consultas =====

    @Transactional(readOnly = true)
    public List<Long> idsFotos(Long eventoId) {
        return fotoRepository.findIdsByEvento(eventoId);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> videos(Long eventoId) {
        return videoRepository.findByEventoIdOrderByIdAsc(eventoId).stream()
                .map(v -> VideoResponse.de(v.getId(), v.getYoutubeId(), v.getTitulo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Imagen obtenerFoto(Long eventoId, Long fotoId) {
        FotoEvento foto = fotoRepository.findByIdAndEventoId(fotoId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada"));
        return new Imagen(foto.getTipoContenido(), foto.getDatos());
    }

    // ===== Fotos =====

    @Transactional
    public Long subirFoto(Long eventoId, String username, MultipartFile archivo) {
        Evento evento = buscarEvento(eventoId);
        if (fotoRepository.countByEventoId(eventoId) >= MAX_FOTOS_POR_EVENTO) {
            throw new ValidacionException("El evento ya tiene el máximo de " + MAX_FOTOS_POR_EVENTO + " fotos");
        }
        Imagen imagen = validarImagen(archivo);
        FotoEvento foto = new FotoEvento(evento, buscarUsuario(username), imagen.tipoContenido(), imagen.datos());
        return fotoRepository.save(foto).getId();
    }

    @Transactional
    public void eliminarFoto(Long eventoId, Long fotoId) {
        FotoEvento foto = fotoRepository.findByIdAndEventoId(fotoId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada"));
        fotoRepository.delete(foto);
    }

    Imagen validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ValidacionException("Selecciona una imagen");
        }
        if (archivo.getSize() > FotoEvento.TAMANO_MAXIMO) {
            throw new ValidacionException("La imagen supera el tamaño máximo de 2 MB");
        }

        byte[] original;
        try {
            original = archivo.getBytes();
        } catch (IOException e) {
            throw new ValidacionException("No fue posible leer la imagen");
        }

        String formato = detectarFormato(original);
        if (formato == null) {
            throw new ValidacionException("Solo se permiten imágenes JPG o PNG");
        }

        BufferedImage imagen = decodificar(original, formato);
        byte[] limpia = recodificar(imagen, formato);
        if (limpia.length > FotoEvento.TAMANO_MAXIMO) {
            throw new ValidacionException("La imagen supera el tamaño máximo de 2 MB");
        }
        return new Imagen("png".equals(formato) ? "image/png" : "image/jpeg", limpia);
    }

    // Firma ("magic bytes") de los formatos permitidos
    private static String detectarFormato(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G'
                && d[4] == 0x0D && d[5] == 0x0A && d[6] == 0x1A && d[7] == 0x0A) {
            return "png";
        }
        return null;
    }

    private static BufferedImage decodificar(byte[] datos, String formato) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReadersByFormatName(formato);
            if (entrada == null || !lectores.hasNext()) {
                throw new ValidacionException("La imagen no es válida");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(entrada, true, true);
                // Se revisan las dimensiones ANTES de decodificar la imagen completa en memoria
                if (lector.getWidth(0) > MAX_DIMENSION || lector.getHeight(0) > MAX_DIMENSION) {
                    throw new ValidacionException("La imagen supera las dimensiones máximas de "
                            + MAX_DIMENSION + " x " + MAX_DIMENSION + " px");
                }
                BufferedImage imagen = lector.read(0);
                if (imagen == null) {
                    throw new ValidacionException("La imagen no es válida");
                }
                return imagen;
            } finally {
                lector.dispose();
            }
        } catch (ValidacionException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            // Archivo dañado o que solo aparenta ser una imagen
            throw new ValidacionException("La imagen no es válida o está dañada");
        }
    }

    private static byte[] recodificar(BufferedImage imagen, String formato) {
        BufferedImage salida = imagen;
        if ("jpeg".equals(formato) && imagen.getColorModel().hasAlpha()) {
            // JPEG no admite transparencia: se convierte a RGB
            salida = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D lienzo = salida.createGraphics();
            try {
                lienzo.drawImage(imagen, 0, 0, java.awt.Color.WHITE, null);
            } finally {
                lienzo.dispose();
            }
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(salida, "png".equals(formato) ? "png" : "jpg", out)) {
                throw new ValidacionException("La imagen no es válida");
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new ValidacionException("No fue posible procesar la imagen");
        }
    }

    // ===== Videos =====

    @Transactional
    public Long agregarVideo(Long eventoId, String username, String url, String titulo) {
        Evento evento = buscarEvento(eventoId);
        if (videoRepository.countByEventoId(eventoId) >= MAX_VIDEOS_POR_EVENTO) {
            throw new ValidacionException("El evento ya tiene el máximo de " + MAX_VIDEOS_POR_EVENTO + " videos");
        }
        String youtubeId = extraerIdYoutube(url);
        String tituloFinal = StringUtils.hasText(titulo) ? titulo.trim() : "Video del evento";
        if (tituloFinal.length() > 100) {
            throw new ValidacionException("El título no puede superar los 100 caracteres");
        }
        VideoEvento video = new VideoEvento(evento, buscarUsuario(username), youtubeId, tituloFinal);
        return videoRepository.save(video).getId();
    }

    @Transactional
    public void eliminarVideo(Long eventoId, Long videoId) {
        VideoEvento video = videoRepository.findByIdAndEventoId(videoId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video no encontrado"));
        videoRepository.delete(video);
    }

    // Lista blanca: solo se aceptan enlaces de YouTube y se guarda únicamente el ID del video
    static String extraerIdYoutube(String url) {
        if (!StringUtils.hasText(url) || url.length() > 300) {
            throw new ValidacionException("Ingresa un enlace de YouTube válido");
        }
        Matcher m = YOUTUBE.matcher(url.trim());
        if (!m.matches()) {
            throw new ValidacionException("Solo se permiten enlaces de YouTube (youtube.com o youtu.be)");
        }
        return m.group(1);
    }

    // ===== Utilidades =====

    private Evento buscarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }
}
