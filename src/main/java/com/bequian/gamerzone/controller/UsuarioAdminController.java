package com.bequian.gamerzone.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bequian.gamerzone.api.dto.UsuarioResponse;
import com.bequian.gamerzone.model.Usuario;
import com.bequian.gamerzone.model.UsuarioForm;
import com.bequian.gamerzone.service.GestionUsuarioService;
import com.bequian.gamerzone.service.ValidacionException;

// [Privada - ADMIN] Gestión de usuarios y administradores desde el panel web
@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {

    private static final String VISTA_FORM = "admin/usuario-form";
    private static final String REDIRECT_LISTA = "redirect:/admin/usuarios";

    private final GestionUsuarioService gestionUsuarioService;

    public UsuarioAdminController(GestionUsuarioService gestionUsuarioService) {
        this.gestionUsuarioService = gestionUsuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        // Se envía el DTO (sin hash de contraseña) con los roles ya ordenados
        model.addAttribute("usuarios", gestionUsuarioService.listar().stream().map(UsuarioResponse::desde).toList());
        model.addAttribute("totalAdmins", gestionUsuarioService.contarAdministradores());
        return "admin/usuarios";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        UsuarioForm form = new UsuarioForm();
        form.getRoles().add(GestionUsuarioService.ROL_USER);
        return mostrarFormulario(model, form, null, null);
    }

    @PostMapping("/nuevo")
    public String crear(@ModelAttribute("usuario") UsuarioForm form, Model model, RedirectAttributes flash) {
        try {
            Usuario creado = gestionUsuarioService.crear(form.getNombre(), form.getCorreo(),
                    form.getPassword(), form.getRoles());
            flash.addFlashAttribute("mensaje", "Usuario " + creado.getUsername() + " creado");
            return REDIRECT_LISTA;
        } catch (ValidacionException e) {
            form.setPassword(null);   // nunca se devuelve la contraseña a la vista
            return mostrarFormulario(model, form, null, e.getMessage());
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return mostrarFormulario(model, UsuarioForm.desde(gestionUsuarioService.obtener(id)), id, null);
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Long id, @ModelAttribute("usuario") UsuarioForm form,
                             Principal principal, Model model, RedirectAttributes flash) {
        try {
            Usuario actualizado = gestionUsuarioService.actualizar(id, form.getNombre(), form.getRoles(),
                    form.getPassword(), principal.getName());
            flash.addFlashAttribute("mensaje", "Usuario " + actualizado.getUsername() + " actualizado");
            return REDIRECT_LISTA;
        } catch (ValidacionException e) {
            form.setPassword(null);
            form.setCorreo(gestionUsuarioService.obtener(id).getUsername());
            return mostrarFormulario(model, form, id, e.getMessage());
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        try {
            gestionUsuarioService.eliminar(id, principal.getName());
            flash.addFlashAttribute("mensaje", "Usuario eliminado");
        } catch (ValidacionException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_LISTA;
    }

    private String mostrarFormulario(Model model, UsuarioForm form, Long id, String error) {
        model.addAttribute("usuario", form);
        model.addAttribute("usuarioId", id);
        model.addAttribute("rolesDisponibles", GestionUsuarioService.ROLES);
        if (error != null) {
            model.addAttribute("error", error);
        }
        return VISTA_FORM;
    }
}
