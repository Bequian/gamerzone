package com.bequian.gamerzone.service;

// Error de validación de negocio (imagen inválida, enlace no permitido, límite alcanzado, etc.).
// El mensaje es seguro para mostrarlo al usuario: nunca incluye detalles internos.
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
