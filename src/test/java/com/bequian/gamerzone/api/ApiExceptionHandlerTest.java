package com.bequian.gamerzone.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.api.dto.ErrorResponse;
import com.bequian.gamerzone.service.ValidacionException;

// Pruebas unitarias del manejador de errores de la API (sin levantar Spring)
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    // Método auxiliar: solo se usa para construir el MethodParameter de la excepción de validación
    @SuppressWarnings("unused")
    private void metodoDePrueba(String dato) {
        // sin cuerpo
    }

    @Test
    void validacionDevuelve400ConElPrimerErrorDeCadaCampo() throws Exception {
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "evento");
        resultado.addError(new FieldError("evento", "titulo", "El título es obligatorio"));
        resultado.addError(new FieldError("evento", "titulo", "Segundo error del mismo campo"));
        MethodParameter parametro = new MethodParameter(
                ApiExceptionHandlerTest.class.getDeclaredMethod("metodoDePrueba", String.class), 0);

        ResponseEntity<ErrorResponse> respuesta =
                handler.validacion(new MethodArgumentNotValidException(parametro, resultado));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        assertThat(respuesta.getBody().errores()).containsEntry("titulo", "El título es obligatorio").hasSize(1);
    }

    @Test
    void estadoUsaLaRazonOElTextoEstandar() {
        assertThat(handler.estado(new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"))
                .getBody().mensaje()).isEqualTo("Evento no encontrado");
        // Sin razón: se usa el texto estándar del código HTTP
        ResponseEntity<ErrorResponse> sinRazon = handler.estado(new ResponseStatusException(HttpStatus.FORBIDDEN));
        assertThat(sinRazon.getStatusCode().value()).isEqualTo(403);
        assertThat(sinRazon.getBody().mensaje()).isEqualTo("Forbidden");
    }

    @Test
    void erroresDeNegocioFormatoYArchivoDevuelven400() {
        assertThat(handler.negocio(new ValidacionException("Límite alcanzado")).getBody().mensaje())
                .isEqualTo("Límite alcanzado");
        assertThat(handler.formatoInvalido(new IllegalArgumentException("detalle interno")).getBody().mensaje())
                .isEqualTo("Formato de la solicitud inválido");   // no expone el detalle interno
        assertThat(handler.archivoInvalido(new MultipartException("x")).getStatusCode().value()).isEqualTo(400);
    }
}
