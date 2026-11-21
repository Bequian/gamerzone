package com.bequian.gamerzone.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

// Pruebas unitarias de las validaciones de MultimediaService (sin levantar Spring ni la BD)
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
}
