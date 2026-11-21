package com.bequian.gamerzone.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import com.bequian.gamerzone.api.dto.ErrorResponse;
import com.bequian.gamerzone.service.ValidacionException;

// Convierte los errores de la API en respuestas JSON claras, sin exponer detalles internos.
// Solo aplica a los controladores del paquete "api".
@RestControllerAdvice(basePackageClasses = ApiExceptionHandler.class)
public class ApiExceptionHandler {

    // 400: datos que no cumplen las validaciones (@NotBlank, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Datos inválidos", errores));
    }

    // 400: JSON mal formado o con tipos incorrectos (por ejemplo una fecha inválida)
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> formatoInvalido(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Formato de la solicitud inválido"));
    }

    // 404 y otros códigos lanzados con ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> estado(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        String mensaje = e.getReason() != null ? e.getReason() : HttpStatus.valueOf(status).getReasonPhrase();
        return ResponseEntity.status(status).body(new ErrorResponse(status, mensaje));
    }

    // 400: reglas de negocio (imagen inválida, enlace que no es de YouTube, límite de fotos, etc.)
    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> negocio(ValidacionException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
    }

    // 400: falta el archivo o la solicitud multipart está mal formada
    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> archivoInvalido(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "Debes enviar una imagen en el campo \"archivo\""));
    }
}
