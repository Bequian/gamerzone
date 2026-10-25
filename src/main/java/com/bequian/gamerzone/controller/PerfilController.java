package com.bequian.gamerzone.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bequian.gamerzone.model.Catalogo;
import com.bequian.gamerzone.model.PerfilForm;
import com.bequian.gamerzone.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Muestra el formulario de edición de perfil con los datos actuales del usuario
    @GetMapping
    public String ver(Authentication auth, Model model) {
        String username = auth.getName();
        model.addAttribute("perfilForm", PerfilForm.desde(usuarioService.obtenerPerfil(username)));
        cargarDatos(model, username);
        return "perfil";
    }

    // Guarda los cambios del formulario de perfil, validando los datos y mostrando errores si es necesario
    @PostMapping
    public String guardar(@Valid @ModelAttribute("perfilForm") PerfilForm form,
                          BindingResult result,
                          Authentication auth,
                          Model model,
                          RedirectAttributes redirect) {

        // whitelist de avatares y juegos permitidos
        if (form.getAvatar() != null && !Catalogo.AVATARES.containsKey(form.getAvatar())) {
            result.rejectValue("avatar", "invalido", "Selecciona un avatar válido");
        }
        if (!Catalogo.JUEGOS.containsAll(form.getJuegos())) {
            result.rejectValue("juegos", "invalido", "Selección de juegos no válida");
        }

        String username = auth.getName();
        if (result.hasErrors()) {
            cargarDatos(model, username);
            return "perfil";
        }

        usuarioService.guardarPerfil(username, form.aPerfil());
        redirect.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");
        return "redirect:/perfil";
    }

    private void cargarDatos(Model model, String username) {
        model.addAttribute("perfil", usuarioService.obtenerPerfil(username));
        model.addAttribute("nombre", usuarioService.nombreDe(username));
        model.addAttribute("avatares", Catalogo.AVATARES);
        model.addAttribute("juegosDisponibles", Catalogo.JUEGOS);
    }
}