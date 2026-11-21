package com.bequian.gamerzone.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;

// Pruebas unitarias del manejo de archivos que superan el límite de subida
class SubidaArchivoExceptionHandlerTest {

    private final SubidaArchivoExceptionHandler handler = new SubidaArchivoExceptionHandler();

    private static MockHttpServletRequest peticion(String ruta) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ruta);
        request.setAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
        return request;
    }

    @Test
    void sitioWebVuelveAlDetalleConMensaje() throws Exception {
        MockHttpServletRequest request = peticion("/eventos/7/fotos");
        ModelAndView vista = handler.archivoDemasiadoGrande(request, new MockHttpServletResponse());

        assertThat(vista.getViewName()).isEqualTo("redirect:/eventos/7#multimedia");
        FlashMap flash = (FlashMap) request.getAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE);
        // FlashMap es Map y Comparable a la vez: se compara el valor directamente para evitar ambigüedad
        assertThat(flash.get("errorMultimedia")).isEqualTo("La imagen supera el tamaño máximo de 2 MB");
    }

    @Test
    void rutaDesconocidaNoPermiteRedireccionAbierta() throws Exception {
        ModelAndView vista = handler.archivoDemasiadoGrande(peticion("/otra/ruta"), new MockHttpServletResponse());
        assertThat(vista.getViewName()).isEqualTo("redirect:/eventos#multimedia");
    }

    @Test
    void apiRespondeJson413() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView vista = handler.archivoDemasiadoGrande(peticion("/api/eventos/7/fotos"), response);

        assertThat(vista.isEmpty()).isTrue();
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentAsString()).contains("\"status\":413");
    }
}
