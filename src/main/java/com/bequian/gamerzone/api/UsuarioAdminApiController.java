package com.bequian.gamerzone.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bequian.gamerzone.api.dto.UsuarioActualizarRequest;
import com.bequian.gamerzone.api.dto.UsuarioRequest;
import com.bequian.gamerzone.api.dto.UsuarioResponse;
import com.bequian.gamerzone.service.GestionUsuarioService;

import jakarta.validation.Valid;

// [Privada - ADMIN] API REST de gestión de usuarios (requiere token JWT de un administrador)
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminApiController {

    private final GestionUsuarioService gestionUsuarioService;

    public UsuarioAdminApiController(GestionUsuarioService gestionUsuarioService) {
        this.gestionUsuarioService = gestionUsuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return gestionUsuarioService.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.desde(gestionUsuarioService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        var creado = gestionUsuarioService.crear(request.nombre(), request.correo(),
                request.password(), request.roles());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioActualizarRequest request,
                                      Principal principal) {
        return UsuarioResponse.desde(gestionUsuarioService.actualizar(id, request.nombre(), request.roles(),
                request.password(), principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Principal principal) {
        gestionUsuarioService.eliminar(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
