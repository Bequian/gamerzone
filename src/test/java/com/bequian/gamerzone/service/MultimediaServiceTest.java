package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.model.Evento;
import com.bequian.gamerzone.model.FotoEvento;
import com.bequian.gamerzone.model.ServiciosEvento;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.model.VideoEvento;
import com.bequian.gamerzone.repository.EventoRepository;
import com.bequian.gamerzone.repository.FotoEventoRepository;
import com.bequian.gamerzone.repository.UsuarioRepository;
import com.bequian.gamerzone.repository.VideoEventoRepository;

// Pruebas unitarias de MultimediaService (sin levantar Spring ni la BD; los repositorios se simulan con Mockito)
class MultimediaServiceTest {

    // La validación de imágenes y enlaces no usa los repositorios
    private final MultimediaService servicio = new MultimediaService(null, null, null, null);

    private static byte[] imagen(int ancho, int alto, String formato) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB), formato, salida);
        return salida.toByteArray();
    }

    private static MockMultipartFile archivo(byte[] datos) {
        return new MockMultipartFile("archivo", "foto", "image/png", datos);
    }

    // ===== Enlaces de YouTube =====

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?feature=share&v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=42",
            "https://m.youtube.com/shorts/dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ"
    })
    void aceptaEnlacesDeYoutube(String url) {
        assertThat(MultimediaService.extraerIdYoutube(url)).isEqualTo("dQw4w9WgXcQ");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://evil.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com.evil.com/watch?v=dQw4w9WgXcQ",
            "javascript:alert(1)",
            "https://www.youtube.com/watch?v=corto",
            "https://youtu.be/dQw4w9WgXcQ\"><script>alert(1)</script>"
    })
    void rechazaEnlacesNoPermitidos(String url) {
        assertThatThrownBy(() -> MultimediaService.extraerIdYoutube(url))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void rechazaEnlaceVacioODemasiadoLargo() {
        assertThatThrownBy(() -> MultimediaService.extraerIdYoutube("  "))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> MultimediaService.extraerIdYoutube(null))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> MultimediaService.extraerIdYoutube("https://youtu.be/" + "a".repeat(300)))
                .isInstanceOf(ValidacionException.class);
    }

    // ===== Validación de imágenes =====

    @Test
    void aceptaPngYJpegYLosRecodifica() throws IOException {
        MultimediaService.Imagen png = servicio.validarImagen(archivo(imagen(30, 20, "png")));
        assertThat(png.tipoContenido()).isEqualTo("image/png");
        assertThat(ImageIO.read(new ByteArrayInputStream(png.datos())).getWidth()).isEqualTo(30);

        MultimediaService.Imagen jpeg = servicio.validarImagen(archivo(imagen(30, 20, "jpg")));
        assertThat(jpeg.tipoContenido()).isEqualTo("image/jpeg");
        assertThat(ImageIO.read(new ByteArrayInputStream(jpeg.datos())).getHeight()).isEqualTo(20);
    }

    @Test
    void rechazaArchivoAusenteOVacio() {
        assertThatThrownBy(() -> servicio.validarImagen(null))
                .isInstanceOf(ValidacionException.class).hasMessage("Selecciona una imagen");
        assertThatThrownBy(() -> servicio.validarImagen(archivo(new byte[0])))
                .isInstanceOf(ValidacionException.class).hasMessage("Selecciona una imagen");
    }

    @Test
    void rechazaArchivoQueNoEsImagen() {
        assertThatThrownBy(() -> servicio.validarImagen(archivo("<html>hola</html>".getBytes())))
                .isInstanceOf(ValidacionException.class).hasMessage("Solo se permiten imágenes JPG o PNG");
    }

    @Test
    void rechazaArchivoConFirmaValidaPeroContenidoFalso() {
        // Empieza como PNG / JPEG (firma correcta) pero el resto no es una imagen
        byte[] pngFalso = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4, 5};
        byte[] jpegFalso = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 1, 2, 3};
        assertThatThrownBy(() -> servicio.validarImagen(archivo(pngFalso)))
                .isInstanceOf(ValidacionException.class).hasMessage("La imagen no es válida o está dañada");
        assertThatThrownBy(() -> servicio.validarImagen(archivo(jpegFalso)))
                .isInstanceOf(ValidacionException.class).hasMessage("La imagen no es válida o está dañada");
    }

    @Test
    void rechazaImagenConDimensionesExcesivas() throws IOException {
        byte[] enorme = imagen(6001, 1, "png");
        assertThatThrownBy(() -> servicio.validarImagen(archivo(enorme)))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("dimensiones máximas");
    }

    @Test
    void rechazaImagenMayorA2MB() {
        byte[] grande = new byte[2 * 1024 * 1024 + 1];
        assertThatThrownBy(() -> servicio.validarImagen(archivo(grande)))
                .isInstanceOf(ValidacionException.class).hasMessage("La imagen supera el tamaño máximo de 2 MB");
    }

    // ===== Record Imagen (regla java:S6218) =====

    @Test
    void imagenComparaPorContenidoYNoImprimeLosBytes() {
        MultimediaService.Imagen a = new MultimediaService.Imagen("image/png", new byte[] {1, 2, 3});
        MultimediaService.Imagen b = new MultimediaService.Imagen("image/png", new byte[] {1, 2, 3});
        MultimediaService.Imagen c = new MultimediaService.Imagen("image/png", new byte[] {9});

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(c).isNotEqualTo("otro objeto");
        assertThat(a.toString()).isEqualTo("Imagen[tipoContenido=image/png, bytes=3]");
    }
    @Test
    void imagenConDistintoTipoNoEsIgual() {
        MultimediaService.Imagen png = new MultimediaService.Imagen("image/png", new byte[] {1});
        assertThat(png).isNotEqualTo(new MultimediaService.Imagen("image/jpeg", new byte[] {1}));
    }

    // ===== Firmas ("magic bytes") incompletas o alteradas =====

    static Stream<byte[]> firmasInvalidas() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        Stream.Builder<byte[]> casos = Stream.builder();
        // PNG con cada uno de sus 8 bytes de firma alterado
        for (int i = 0; i < png.length; i++) {
            byte[] alterado = png.clone();
            alterado[i] = 0x00;
            casos.add(alterado);
        }
        // JPEG incompleto o alterado
        casos.add(new byte[] {(byte) 0xFF, (byte) 0xD8});
        casos.add(new byte[] {(byte) 0xFF, 0x00, (byte) 0xFF});
        casos.add(new byte[] {(byte) 0xFF, (byte) 0xD8, 0x00});
        casos.add(new byte[] {(byte) 0x89, 'P', 'N'});
        return casos.build();
    }

    @ParameterizedTest
    @MethodSource("firmasInvalidas")
    void rechazaFirmasIncompletasOAlteradas(byte[] datos) {
        assertThatThrownBy(() -> servicio.validarImagen(archivo(datos)))
                .isInstanceOf(ValidacionException.class).hasMessage("Solo se permiten imágenes JPG o PNG");
    }

    @Test
    void rechazaImagenDemasiadoAlta() throws IOException {
        byte[] alta = imagen(1, 6001, "png");
        assertThatThrownBy(() -> servicio.validarImagen(archivo(alta)))
                .isInstanceOf(ValidacionException.class).hasMessageContaining("dimensiones máximas");
    }

    @Test
    void errorAlLeerElArchivoSeInformaSinDetallesInternos() throws IOException {
        MultipartFile roto = mock(MultipartFile.class);
        when(roto.isEmpty()).thenReturn(false);
        when(roto.getSize()).thenReturn(10L);
        when(roto.getBytes()).thenThrow(new IOException("disco dañado"));

        assertThatThrownBy(() -> servicio.validarImagen(roto))
                .isInstanceOf(ValidacionException.class).hasMessage("No fue posible leer la imagen");
    }

    // ===== Operaciones con repositorios simulados =====

    private final FotoEventoRepository fotoRepository = mock(FotoEventoRepository.class);
    private final VideoEventoRepository videoRepository = mock(VideoEventoRepository.class);
    private final EventoRepository eventoRepository = mock(EventoRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final MultimediaService conRepos =
            new MultimediaService(fotoRepository, videoRepository, eventoRepository, usuarioRepository);

    private final Evento evento = new Evento("LAN", "LAN Party", "Descripción", LocalDate.of(2030, 1, 1),
            LocalTime.NOON, "Santiago", "Calle 1", List.of("GamerZone"),
            new ServiciosEvento(true, true, false, false), List.of(), false);
    private final Usuario organizador = new Usuario("organizador", "hash", "Organizador", Set.of("USER", "ORGANIZADOR"));

    private void eventoYUsuarioExisten() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(usuarioRepository.findByUsername("organizador")).thenReturn(Optional.of(organizador));
    }

    @Test
    void videoSinTituloUsaUnoPorDefecto() {
        eventoYUsuarioExisten();
        when(videoRepository.save(any(VideoEvento.class))).thenAnswer(i -> i.getArgument(0));

        conRepos.agregarVideo(1L, "organizador", "https://youtu.be/dQw4w9WgXcQ", "  ");

        verify(videoRepository).save(org.mockito.ArgumentMatchers.<VideoEvento>argThat(
                v -> v.getTitulo().equals("Video del evento") && v.getYoutubeId().equals("dQw4w9WgXcQ")));
    }

    @Test
    void videoConTituloLargoOLimiteAlcanzadoSeRechaza() {
        eventoYUsuarioExisten();
        String largo = "t".repeat(101);
        assertThatThrownBy(() -> conRepos.agregarVideo(1L, "organizador", "https://youtu.be/dQw4w9WgXcQ", largo))
                .isInstanceOf(ValidacionException.class).hasMessageContaining("100 caracteres");

        when(videoRepository.countByEventoId(1L)).thenReturn((long) MultimediaService.MAX_VIDEOS_POR_EVENTO);
        assertThatThrownBy(() -> conRepos.agregarVideo(1L, "organizador", "https://youtu.be/dQw4w9WgXcQ", "x"))
                .isInstanceOf(ValidacionException.class).hasMessageContaining("máximo");
    }

    @Test
    void limiteDeFotosPorEvento() {
        eventoYUsuarioExisten();
        when(fotoRepository.countByEventoId(1L)).thenReturn((long) MultimediaService.MAX_FOTOS_POR_EVENTO);
        assertThatThrownBy(() -> conRepos.subirFoto(1L, "organizador", archivo(new byte[] {1})))
                .isInstanceOf(ValidacionException.class).hasMessageContaining("máximo");
    }

    @Test
    void eventoUsuarioFotoOVideoInexistentesDevuelvenError() throws IOException {
        byte[] png = imagen(4, 4, "png");
        assertThatThrownBy(() -> conRepos.subirFoto(99L, "organizador", archivo(png)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Evento no encontrado");

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        assertThatThrownBy(() -> conRepos.subirFoto(1L, "fantasma", archivo(png)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Usuario no válido");

        assertThatThrownBy(() -> conRepos.obtenerFoto(1L, 5L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> conRepos.eliminarFoto(1L, 5L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> conRepos.eliminarVideo(1L, 5L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void obtenerYEliminarFotoYVideoExistentes() {
        FotoEvento foto = new FotoEvento(evento, organizador, "image/png", new byte[] {7});
        VideoEvento video = new VideoEvento(evento, organizador, "dQw4w9WgXcQ", "Tráiler");
        when(fotoRepository.findByIdAndEventoId(5L, 1L)).thenReturn(Optional.of(foto));
        when(videoRepository.findByIdAndEventoId(6L, 1L)).thenReturn(Optional.of(video));
        when(fotoRepository.findIdsByEvento(1L)).thenReturn(List.of(5L));
        when(videoRepository.findByEventoIdOrderByIdAsc(1L)).thenReturn(List.of(video));

        assertThat(conRepos.obtenerFoto(1L, 5L)).isEqualTo(new MultimediaService.Imagen("image/png", new byte[] {7}));
        assertThat(conRepos.idsFotos(1L)).containsExactly(5L);
        assertThat(conRepos.videos(1L)).singleElement()
                .satisfies(v -> assertThat(v.embedUrl()).endsWith("/embed/dQw4w9WgXcQ"));

        conRepos.eliminarFoto(1L, 5L);
        conRepos.eliminarVideo(1L, 6L);
        verify(fotoRepository).delete(foto);
        verify(videoRepository).delete(video);
    }
}
