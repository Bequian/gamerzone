package com.bequian.gamerzone.controller;

import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bequian.gamerzone.model.RegistroForm;
import com.bequian.gamerzone.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Mostrar el formulario de registro
    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("registro", new RegistroForm());
        return "registro";
    }


    // Procesa el formulario de registro
    @PostMapping
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form,
                            BindingResult result) {

        if (!Objects.equals(form.getPassword(), form.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "noCoincide", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "registro";
        }

        if (!usuarioService.registrar(form)) {
            result.rejectValue("correo", "duplicado", "Ya existe una cuenta con este correo");
            return "registro";
        }

        // Redirigir al login con un parámetro que indica que el registro fue exitoso
        return "redirect:/login?registrado";
    }
}