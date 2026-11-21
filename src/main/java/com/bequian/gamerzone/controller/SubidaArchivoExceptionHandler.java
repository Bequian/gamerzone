package com.bequian.gamerzone.controller;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Archivos que superan el límite de subida (spring.servlet.multipart.max-file-size).
 * El error ocurre antes de llegar al controlador, por eso se maneja aquí:
 *  - API: responde 413 en JSON.
 *  - Sitio web: vuelve al detalle del evento con un mensaje (sin mostrar detalles internos).
 */
@ControllerAdvice
public class SubidaArchivoExceptionHandler {

    private static final String MENSAJE = "La imagen supera el tamaño máximo de 2 MB";
    private static final Pattern RUTA_FOTOS = Pattern.compile("^/eventos/(\\d+)/fotos$");

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView archivoDemasiadoGrande(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());

        if (ruta.startsWith("/api/")) {
            response.setStatus(413);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":413,\"mensaje\":\"" + MENSAJE + "\"}");
            return new ModelAndView();   // respuesta ya escrita
        }

        // Solo se redirige a una ruta interna construida con el ID numérico (evita redirecciones abiertas)
        Matcher m = RUTA_FOTOS.matcher(ruta);
        String destino = m.matches() ? "/eventos/" + m.group(1) : "/eventos";
        // El mensaje viaja como "flash attribute" (RedirectView lo guarda al redirigir)
        FlashMap flash = RequestContextUtils.getOutputFlashMap(request);
        flash.put("errorMultimedia", MENSAJE);
        return new ModelAndView("redirect:" + destino + "#multimedia");
    }
}
